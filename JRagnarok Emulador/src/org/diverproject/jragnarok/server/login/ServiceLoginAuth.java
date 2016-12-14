package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokConstants.DATE_FORMAT;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.binToHex;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.loginMessage;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Encrypt;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LOGIN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LOGIN_MD5MAC;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LOGIN_MD5;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LOGIN_MD5INFO;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LOGIN_HAN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LOGIN_PCBANG;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LOGIN_SSO;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.TimerType.TIMER_INVALID;
import static org.diverproject.jragnarok.server.common.AuthResult.OK;
import static org.diverproject.jragnarok.server.common.AuthResult.REJECTED_FROM_SERVER;
import static org.diverproject.jragnarok.server.common.NotifyAuthResult.RECOGNIZES_LAST_LOGIN;
import static org.diverproject.jragnarok.server.common.NotifyAuthResult.SERVER_CLOSED;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnarok.packets.receive.LoginDefault;
import org.diverproject.jragnarok.packets.receive.LoginHan;
import org.diverproject.jragnarok.packets.receive.LoginMD5;
import org.diverproject.jragnarok.packets.receive.LoginMD5Info;
import org.diverproject.jragnarok.packets.receive.LoginMD5Mac;
import org.diverproject.jragnarok.packets.receive.LoginPCBang;
import org.diverproject.jragnarok.packets.receive.LoginSingleSignOn;
import org.diverproject.jragnarok.packets.request.AlreadyOnline;
import org.diverproject.jragnarok.packets.request.CharServerConnectRequest;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.ServerState;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.common.AuthResult;
import org.diverproject.jragnarok.server.common.CharServerType;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.Time;
import org.diverproject.util.lang.IntUtil;

/**
 * <h1>Servi�o para Autenticar Acesso</h1>
 *
 * <p>Esse servi�o dever� ser respons�vel pela autentica��o dos dados recebidos de um cliente.
 * Far� o contato com o banco de dados solicitando os dados para realizar a devida autentica��o.
 * Caso haja problemas durante a autentica��o dever� repassar ao cliente os problemas encontrados.
 * Tamb�m fica de sua conta reconhecer o tipo de acesso que o cliente est� solicitando.</p>
 *
 * @see AbstractServiceLogin
 * @see ServiceLoginServer
 * @see ServiceLoginChar
 * @see ServiceLoginClient
 * @see ServiceLoginIpBan
 * @see ServiceLoginLog
 * @see OnlineMap
 * @see AuthAccountMap
 *
 * @author Andrew
 */

public class ServiceLoginAuth extends AbstractServiceLogin
{
	/**
	 * Tempo para que uma autentica��o entre em timeout.
	 */
	private static final int AUTH_TIMEOUT = seconds(30);


	/**
	 * Servi�o para comunica��o entre o servidor e o cliente.
	 */
	private ServiceLoginClient client;

	/**
	 * Servi�o para banimento de acessos por endere�o de IP.
	 */
	private ServiceLoginIpBan ipban;

	/**
	 * Servi�o para registro de acessos.
	 */
	private ServiceLoginLog log;

	/**
	 * Servi�o para acesso de contas (servi�o principal)
	 */
	private ServiceLoginServer login;

	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineMap onlines;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthAccountMap auths;

	/**
	 * Cria um novo servi�o para autentica��o de solicita��es dos acessos ao servidor.
	 * Esse servi�o far� o contato com o banco de dados e autentica��es necess�rias.
	 * @param server refer�ncia do servidor de acesso que deseja criar o servi�o.
	 */

	public ServiceLoginAuth(LoginServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getClientService();
		ipban = getServer().getFacade().getIpBanService();
		log = getServer().getFacade().getLogService();
		login = getServer().getFacade().getLoginService();
		onlines = getServer().getFacade().getOnlineControl();
		auths = getServer().getFacade().getAuthControl();
	}

	@Override
	public void destroy()
	{
		client = null;
		ipban = null;
		log = null;
		login = null;
		onlines = null;
		auths = null;
	}

	/**
	 * Efetua a solicita��o de acesso com o servidor de personagens recebido de um cliente.
	 * Para este caso o cliente j� � reconhecido como um jogador atrav�s do execut�vel.
	 * Dever� receber os dados do cliente adequadamente conforme o tipo de autentica��o.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param command qual o comando que foi executado (tipo de pacote).
	 * @return true se efetuar a an�lise com �xito ou false caso contr�rio.
	 */

	public boolean requestAuth(LFileDescriptor fd, short command)
	{
		boolean usingRawPassword = true;
		LoginSessionData sd = fd.getSessionData();

		switch (command)
		{
			case PACKET_LOGIN:
				LoginDefault loginPacket = new LoginDefault();
				loginPacket.receive(fd, false);
				sd.setVersion(loginPacket.getVersion());
				sd.setClientType(ClientType.parse(loginPacket.getClientType()));
				sd.setUsername(loginPacket.getUsername());
				sd.setPassword(loginPacket.getPassword());
				break;

			case PACKET_LOGIN_PCBANG:
				LoginPCBang loginPCBang = new LoginPCBang();
				loginPCBang.receive(fd, false);
				sd.setVersion(loginPCBang.getVersion());
				sd.setClientType(ClientType.parse(loginPCBang.getClientType()));
				sd.setUsername(loginPCBang.getUsername());
				sd.setPassword(loginPCBang.getPassword());
				break;

			case PACKET_LOGIN_HAN:
				LoginHan loginHan = new LoginHan();
				loginHan.receive(fd, false);
				sd.setVersion(loginHan.getVersion());
				sd.setClientType(ClientType.parse(loginHan.getClientType()));
				sd.setUsername(loginHan.getUsername());
				sd.setPassword(loginHan.getPassword());
				break;

			case PACKET_LOGIN_SSO:
				LoginSingleSignOn loginSingleSignOn = new LoginSingleSignOn();
				loginSingleSignOn.receive(fd, false);
				sd.setVersion(loginSingleSignOn.getVersion());
				sd.setClientType(ClientType.parse(loginSingleSignOn.getClientType()));
				sd.setUsername(loginSingleSignOn.getUsername());
				sd.setPassword(loginSingleSignOn.getToken());
				break;

			case PACKET_LOGIN_MD5:
				LoginMD5 loginMD5 = new LoginMD5();
				loginMD5.receive(fd, false);
				sd.setVersion(loginMD5.getVersion());
				sd.setUsername(loginMD5.getUsername());
				sd.setPassword(loginMD5.getPassword());
				sd.setClientType(ClientType.parse(loginMD5.getClientType()));
				usingRawPassword = false;
				break;

			case PACKET_LOGIN_MD5INFO:
				LoginMD5Info loginMD5Info = new LoginMD5Info();
				loginMD5Info.receive(fd, false);
				sd.setVersion(loginMD5Info.getVersion());
				sd.setUsername(loginMD5Info.getUsername());
				sd.setPassword(loginMD5Info.getPassword());
				sd.setClientType(ClientType.parse(loginMD5Info.getClientType()));
				usingRawPassword = false;
				break;

			case PACKET_LOGIN_MD5MAC:
				LoginMD5Mac loginMD5Mac = new LoginMD5Mac();
				loginMD5Mac.receive(fd, false);
				sd.setVersion(loginMD5Mac.getVersion());
				sd.setUsername(loginMD5Mac.getUsername());
				sd.setPassword(loginMD5Mac.getPassword());
				sd.setClientType(ClientType.parse(loginMD5Mac.getClientType()));
				usingRawPassword = false;
				break;
		}

		return parseRequest(fd, usingRawPassword);
	}

	/**
	 * Procedimento que ir� fazer a conclus�o da autentica��o da solicita��o de um cliente.
	 * Neste momentos os dados passados pelo cliente j� ter�o sido lidos e guardados na sess�o.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param usingRawPassword true se estiver usando senha direta ou false se for md5.
	 * @return true se for autenticado com �xito ou false caso contr�rio.
	 */

	private boolean parseRequest(LFileDescriptor fd, boolean usingRawPassword)
	{
		LoginSessionData sd = fd.getSessionData();

		if (usingRawPassword)
		{
			logNotice("solicita��o de conex�o de %s (ip: %s, version: %d)\n", sd.getUsername(), fd.getAddressString(), sd.getVersion());

			if (getConfigs().getBool("login.use_md5_password"))
				sd.setPassword(md5Encrypt(sd.getPassword()));

			sd.getPassDencrypt().setValue(b(0));
		}

		else
		{
			log("solicita��o de conex�o passdenc de %s (ip: %s, version: %d)\n", sd.getUsername(), fd.getAddressString(), sd.getVersion());

			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT);
			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT2);
			sd.setPassword(binToHex(sd.getPassword(), 16));
		}

		if (sd.getPassDencrypt().getValue() != 0 && getConfigs().getBool("login.use_md5_password"))
		{
			client.sendAuthResult(fd, AuthResult.REJECTED_FROM_SERVER);
			return false;
		}

		AuthResult result = login.parseAuthLogin(fd, false);

		if (result != AuthResult.OK)
		{
			authFailed(fd, result);
			return false;
		}

		authOk(fd);
		return true;
	}

	/**
	 * Chamado internamente sempre que uma solicita��o de acesso tiver falhado na autentica��o.
	 * Deve registrar a falha se habilitado o log e responder ao cliente qual o motivo da falha.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailed(LFileDescriptor fd, AuthResult result)
	{
		authFailedLog(fd, result);
		authFailedResponse(fd, result);
	}

	/**
	 * Registra uma solicita��o de acesso que n�o foi autenticada corretamente.
	 * Esse registro � feito no banco de dados para identificar quem falhou.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailedLog(LFileDescriptor fd, AuthResult result)
	{
		LoginSessionData sd = fd.getSessionData();

		if (getConfigs().getBool("log.login"))
		{
			if (IntUtil.interval(result.CODE, 0, 15))
				log.add(fd.getAddress(), sd, result.CODE, loginMessage(result.CODE));

			else if (IntUtil.interval(result.CODE, 99, 104))
				log.add(fd.getAddress(), sd, result.CODE, loginMessage(result.CODE-83));

			else
				log.add(fd.getAddress(), sd, result.CODE, loginMessage(22));
		}		

		if (result.CODE == 0 || result.CODE == 1)
			ipban.addBanLog(fd.getAddressString());
	}

	/**
	 * Procedimento que ir� responder ao cliente os detalhes do resultado da autentica��o.
	 * Caso o cliente esteja banido no servidor ir� informar at� quando o mesmo ocorre.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailedResponse(LFileDescriptor fd, AuthResult result)
	{
		String blockDate = "";
		LoginSessionData sd = fd.getSessionData();

		if (result == AuthResult.BANNED_UNTIL)
		{
			Account account = (Account) sd.getCache();
			Time unbanTime = account.getUnban();

			if (unbanTime != null && unbanTime.get() > 0)
				blockDate = unbanTime.toStringFormat(DATE_FORMAT);
			else
				blockDate = "! Banido !";
		}

		client.refuseLogin(fd, result, blockDate);
	}

	/**
	 * Chamado internamente sempre que uma solicita��o de acesso tiver sido aprovada na autentica��o.
	 * O segundo passo � verificar a conex�o e estado do servidor, grupos habilitados e se est� online.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	private void authOk(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();

		if (!authServerConnected(fd) || !authServerState(sd) || !authGroupAccount(fd) || !authIsntOnline(fd))
		{
			client.sendNotifyResult(fd, SERVER_CLOSED);
			return;
		}

		Account account = (Account) sd.getCache();

		log.add(new InternetProtocol(fd.getAddress()), account, 100, "login ok");

		logNotice("conex�o da conta '%s' aceita.\n", sd.getUsername());

		client.sendCharServerList(fd);

		AuthNode node = new AuthNode();
		node.setAccountID(sd.getID());
		node.getSeed().copyFrom(sd.getSeed());
		node.getIP().set(fd.getAddress());
		node.setVersion(sd.getVersion());
		node.setClientType(sd.getClientType());
		auths.add(node);

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer waitingDisconnect = timers.acquireTimer();
		waitingDisconnect.setObjectID(sd.getID());
		waitingDisconnect.setTick(ts.getCurrentTime() + AUTH_TIMEOUT);
		waitingDisconnect.setListener(this.login.WAITING_DISCONNECT_TIMER);

		OnlineLogin online = new OnlineLogin();
		online.setAccountID(sd.getID());
		online.setWaitingDisconnect(waitingDisconnect);
		online.setCharServer(OnlineLogin.NONE);
		onlines.add(online, getTimerSystem().getTimers());
	}

	/**
	 * Faz a autentica��o para verificar se h� algum servidor de personagens conectado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se houver ao menos um servidor ou false se n�o houver nenhum
	 */

	private boolean authServerConnected(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();
		Account account = (Account) sd.getCache();
		String username = account.getUsername();

		int serverConnect = 0;
		CharServerList servers = getServer().getCharServerList();

		for (ClientCharServer server : servers)
			if (server.getFileDecriptor().isConnected())
				serverConnect++;

		if (serverConnect == 0)
			logNotice("nenhum servidor de personagens conectado (usernme: %s).\n", username);

		return serverConnect > 0;
	}

	/**
	 * Faz a autentica��o do estado do servidor com o qual est� tentando se conectar.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se estiver rodando ou false caso contr�rio.
	 */

	private boolean authServerState(LoginSessionData sd)
	{
		boolean ok = getServer().getState() == RUNNING;

		if (!ok)
			logNotice("servidor n�o est� rodando (state: %s, usernme: %s).\n", getServer().getState(), sd.getUsername());

		return ok;
	}

	/**
	 * Faz a autentica��o para verificar se o cliente est� contido nos grupos habilitados.
	 * Essa autentica��o s� ser� v�lida caso tenha sido configurado grupos de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se estiver habilitado a conectar-se nesse servidor.
	 */

	private boolean authGroupAccount(LFileDescriptor fd)
	{
		int groupToConnect = getConfigs().getInt("login.group_to_connnect");
		int minGroupToConnect = getConfigs().getInt("login.min_group_to_connect");

		if (groupToConnect == 0 && minGroupToConnect == 0)
			return true;

		Account account = (Account) fd.getSessionData().getCache();

		return	authGroupToConnect(account, groupToConnect) ||
				authMinGroupToConnect(account, groupToConnect, minGroupToConnect);
	}

	/**
	 * Autentica uma conta em rela��o ao �nico grupo de contas habilitado a conectar.
	 * @param account refer�ncia da conta do cliente que dever� ser autenticada.
	 * @param required n�vel da conta necess�ria para se contar.
	 * @return true se estiver habilitado ou false caso contr�rio.
	 */

	private boolean authGroupToConnect(Account account, int required)
	{
		int group = account.getGroup().getCurrentGroup().getID();
		String username = account.getUsername();

		if (required == 0 || group == required)
			return true;

		logNotice("grupo '%d' � o �nico aceito (username: %s, grupoid: %d).\n", required, username, group);

		return false;
	}

	/**
	 * Autentica uma conta para ver se possui n�vel de grupo maior do m�nimo aceito.
	 * Essa verifica��o s� ser� v�lida caso o grupo requerido seja igual a -1.
	 * @param account refer�ncia da conta do cliente que dever� ser autenticada.
	 * @param required n�vel do grupo para se conectar ao servidor (esperado -1).
	 * @param min n�vel do grupo m�nimo aceito para se conectar no servidor.
	 * @return true se tiver um grupo habilitado ou false caso contr�rio.
	 */

	private boolean authMinGroupToConnect(Account account, int required, int min)
	{
		int group = account.getGroup().getCurrentGroup().getID();
		String username = account.getUsername();

		if (required == -1 && min < group)
			return true;

		logNotice("grupo '%d' � o m�nimo aceito (usernme: %s, groupid: %d).\n", min, username, group);

		return false;
	}

	/**
	 * Autentica um cliente verificando se h� um outro cliente usando a conta acessada.
	 * Caso haja um cliente usado a conta, dever� avisar quem est� online e rejeitar o acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se n�o houver ningu�m na conta online ou false caso contr�rio.
	 */

	private boolean authIsntOnline(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();
		Account account = (Account) sd.getCache();
		OnlineLogin online = onlines.get(account.getID());

		if (online != null)
		{
			int id = online.getCharServerID();

			CharServerList servers = getServer().getCharServerList();
			ClientCharServer server = servers.get(id);

			if (server != null)
			{
				authIsOnline(fd, account, online, server);
				return true;
			}

			auths.remove(sd.getID());
			onlines.remove(online, getTimerSystem().getTimers());
		}

		return true;
	}

	/**
	 * A autentica��o do cliente indicou que a conta j� est� sendo usada (online).
	 * Notificar ao cliente de que a conta que o servidor ainda o considera online.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os detalhes da conta que est� sendo acessada.
	 * @param online objeto que cont�m o gatilho para efetuar logout for�ado.
	 * @param server servidor de personagens do qual a conta est� online.
	 */

	private void authIsOnline(LFileDescriptor fd, Account account, OnlineLogin online, ClientCharServer server)
	{
		logNotice("usu�rio '%s' j� est� online em '%s'.\n", account.getUsername(), server.getName());

		AlreadyOnline packet = new AlreadyOnline();
		packet.setAccountID(account.getID());

		client.sendAllWithoutOurSelf(fd, packet);

		if (online.getWaitingDisconnect().getType() == TIMER_INVALID)
		{
			TimerSystem ts = getTimerSystem();

			Timer timer = online.getWaitingDisconnect();
			timer.setObjectID(fd.getID());
			timer.setListener(login.WAITING_DISCONNECT_TIMER);
			timer.setTick(ts.getCurrentTime());
			ts.getTimers().add(timer);

			client.sendNotifyResult(fd, RECOGNIZES_LAST_LOGIN);
		}		
	}

	/**
	 * Chamado quando um servidor de personagens solicita a conex�o com o servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se tiver sido autorizado ou false caso contr�rio.
	 */

	public boolean requestCharConnect(LFileDescriptor fd)
	{
		CharServerConnectRequest ccPacket = new CharServerConnectRequest();
		ccPacket.receive(fd, false);

		// TODO confirmar usu�rio e senha e obter o ID
		LoginSessionData sd = fd.getSessionData();
		sd.setID(1);
		sd.setUsername(ccPacket.getUsername());
		sd.setPassword(ccPacket.getPassword());

		if (getConfigs().getBool("login.user_md5_password"))
			sd.setPassword(md5Encrypt(sd.getPassword()));

		sd.getPassDencrypt().setValue(b(0));
		sd.setVersion(getConfigs().getInt("client.version"));

		String serverName = ccPacket.getServerName();
		int serverIP = ccPacket.getServerIP();
		short serverPort = ccPacket.getServerPort();
		short type = ccPacket.getType();
		boolean newDisplay = ccPacket.getNewDisplay();

		logInfo("conex�o solicitada do servidor de personagens %s@%s (account: %s, pass: %s).\n", serverName, fd.getAddressString(), sd.getUsername(), sd.getPassword());

		String message = format("charserver - %s@%s:%d", serverName, SocketUtil.socketIP(serverIP), serverPort);
		log.add(fd.getAddress(), sd, 100, message);

		AuthResult result = login.parseAuthLogin(fd, true);

		if (getServer().isState(ServerState.RUNNING) && result == AuthResult.OK && fd.isConnected())
		{
			log("conex�o do servidor de personagens '%s' aceita.\n", serverName);

			ClientCharServer server = new ClientCharServer();
			server.setFileDecriptor(fd);
			server.setName(serverName);
			server.getIP().set(serverIP);
			server.setPort(serverPort);
			server.setUsers((short) 0);
			server.setType(CharServerType.parse(type));
			server.setNewDisplay(newDisplay);
			getServer().getCharServerList().add(server);

			fd.setParseListener(getServer().getFacade().PARSE_CHAR_SERVER);
			fd.getFlag().set(FileDescriptor.FLAG_SERVER);

			client.sendCharServerResult(fd, OK);
			return true;
		}

		logNotice("Conex�o com o servidor de personagens '%s' RECUSADA.\n", serverName);

		client.sendCharServerResult(fd, REJECTED_FROM_SERVER);
		return false;
	}
}

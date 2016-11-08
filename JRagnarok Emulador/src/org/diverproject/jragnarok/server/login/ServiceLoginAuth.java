package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokConstants.DATE_FORMAT;
import static org.diverproject.jragnarok.JRagnarokUtil.binToHex;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.loginMessage;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Encrypt;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN2;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN3;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN4;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN_HAN;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN_PCBANG;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_SSO_LOGIN_REQ;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_REQ_CHAR_CONNECT;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.TimerType.TIMER_INVALID;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.OK;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.REJECTED_FROM_SERVER;
import static org.diverproject.jragnarok.server.login.structures.NotifyAuthResult.RECOGNIZES_LAST_LOGIN;
import static org.diverproject.jragnarok.server.login.structures.NotifyAuthResult.SERVER_CLOSED;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnarok.packets.receive.CharConnectReceive;
import org.diverproject.jragnarok.packets.receive.LoginDefault;
import org.diverproject.jragnarok.packets.receive.LoginHan;
import org.diverproject.jragnarok.packets.receive.LoginMD5;
import org.diverproject.jragnarok.packets.receive.LoginMD5Info;
import org.diverproject.jragnarok.packets.receive.LoginMD5Mac;
import org.diverproject.jragnarok.packets.receive.LoginPCBang;
import org.diverproject.jragnarok.packets.receive.LoginSingleSignOn;
import org.diverproject.jragnarok.packets.response.AlreadyOnline;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.ServerState;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.controllers.AuthControl;
import org.diverproject.jragnarok.server.login.controllers.OnlineControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AuthNode;
import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.jragnarok.server.login.entities.OnlineLogin;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.jragnarok.server.login.structures.CharServerType;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.jragnarok.server.login.structures.ClientType;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.Time;
import org.diverproject.util.lang.HexUtil;
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
 * @see OnlineControl
 * @see AuthControl
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
	 * Servi�o para registro dos acessos.
	 */
	private ServiceLoginLog log;

	/**
	 * Servi�o para banimento por endere�o de IP.
	 */
	private ServiceLoginIpBan ipban;

	/**
	 * Servi�o para acesso de clientes com o servidor de acesso.
	 */
	private ServiceLoginServer login;

	/**
	 * Servi�o para acesso de servidores com o servidor de acesso.
	 */
	private ServiceLoginChar character;

	/**
	 * Servi�o para envio de dados para o(s) cliente(s).
	 */
	private ServiceLoginClient client;

	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineControl onlines;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthControl controller;

	/**
	 * Cria um novo servi�o para autentica��o de solicita��es dos acessos ao servidor.
	 * Esse servi�o far� o contato com o banco de dados e autentica��es necess�rias.
	 * @param server refer�ncia do servidor de acesso que deseja criar o servi�o.
	 */

	public ServiceLoginAuth(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa o servi�o para recebimento de novos clientes no servidor.
	 */

	public void init()
	{
		log = getServer().getLogService();
		ipban = getServer().getIpBanService();
		login = getServer().getLoginService();
		character = getServer().getCharService();
		client = getServer().getClientService();

		onlines = new OnlineControl(getTimerSystem().getTimers());
		controller = new AuthControl();
	}

	/**
	 * Limpa as informa��es contidas de usu�rios online e autentica��es feitas.
	 * Ap�s isso destr�i o controlador de usu�rios online e autentica��es feitas.
	 */

	public void destroy()
	{
		onlines.clear();
		controller.clear();

		onlines = null;
		controller = null;
	}

	/**
	 * Despacha um determinado cliente para o seu respectivo tipo de acesso solicitado.
	 * Existem diversos tipos de acessos e cada um deles cont�m informa��es diferentes.
	 * @param command c�digo do pacote que foi recebido do cliente.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se despachar corretamente ou false caso contr�rio.
	 */

	public boolean dispatch(short command, FileDescriptor fd, LoginSessionData sd)
	{
		switch (command)
		{
			// Solicita��o de acesso com senha direta
			case PACKET_CA_LOGIN:
			case PACKET_CA_LOGIN_PCBANG:
			case PACKET_CA_LOGIN_HAN:
			case PACKET_CA_SSO_LOGIN_REQ:
			// Solicita��o de acesso com senha MD5
			case PACKET_CA_LOGIN2:
			case PACKET_CA_LOGIN3:
			case PACKET_CA_LOGIN4:
				return requestAuth(fd, sd, command);

			case PACKET_REQ_CHAR_CONNECT:
				return requestCharConnect(fd, sd);

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logNotice("fim de conex�o inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	};

	/**
	 * Efetua a solicita��o de acesso com o servidor de personagens recebido de um cliente.
	 * Para este caso o cliente j� � reconhecido como um jogador atrav�s do execut�vel.
	 * Dever� receber os dados do cliente adequadamente conforme o tipo de autentica��o.
	 * @param fd refer�ncia da conex�o com o cliente para receber e enviar dados.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @param command qual o comando que foi executado (tipo de pacote).
	 * @return true se efetuar a an�lise com �xito ou false caso contr�rio.
	 */

	private boolean requestAuth(FileDescriptor fd, LoginSessionData sd, short command)
	{
		boolean usingRawPassword = true;

		switch (command)
		{
			case PACKET_CA_LOGIN:
				LoginDefault loginPacket = new LoginDefault();
				loginPacket.receive(fd, false);
				sd.setVersion(loginPacket.getVersion());
				sd.setClientType(ClientType.parse(loginPacket.getClientType()));
				sd.setUsername(loginPacket.getUsername());
				sd.setPassword(loginPacket.getPassword());
				break;

			case PACKET_CA_LOGIN_PCBANG:
				LoginPCBang loginPCBang = new LoginPCBang();
				loginPCBang.receive(fd, false);
				sd.setVersion(loginPCBang.getVersion());
				sd.setClientType(ClientType.parse(loginPCBang.getClientType()));
				sd.setUsername(loginPCBang.getUsername());
				sd.setPassword(loginPCBang.getPassword());
				break;

			case PACKET_CA_LOGIN_HAN:
				LoginHan loginHan = new LoginHan();
				loginHan.receive(fd, false);
				sd.setVersion(loginHan.getVersion());
				sd.setClientType(ClientType.parse(loginHan.getClientType()));
				sd.setUsername(loginHan.getUsername());
				sd.setPassword(loginHan.getPassword());
				break;

			case PACKET_CA_SSO_LOGIN_REQ:
				LoginSingleSignOn loginSingleSignOn = new LoginSingleSignOn();
				loginSingleSignOn.receive(fd, false);
				sd.setVersion(loginSingleSignOn.getVersion());
				sd.setClientType(ClientType.parse(loginSingleSignOn.getClientType()));
				sd.setUsername(loginSingleSignOn.getUsername());
				sd.setPassword(loginSingleSignOn.getToken());
				break;

			case PACKET_CA_LOGIN2:
				LoginMD5 loginMD5 = new LoginMD5();
				loginMD5.receive(fd, false);
				sd.setVersion(loginMD5.getVersion());
				sd.setUsername(loginMD5.getUsername());
				sd.setPassword(loginMD5.getPassword());
				sd.setClientType(ClientType.parse(loginMD5.getClientType()));
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN3:
				LoginMD5Info loginMD5Info = new LoginMD5Info();
				loginMD5Info.receive(fd, false);
				sd.setVersion(loginMD5Info.getVersion());
				sd.setUsername(loginMD5Info.getUsername());
				sd.setPassword(loginMD5Info.getPassword());
				sd.setClientType(ClientType.parse(loginMD5Info.getClientType()));
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN4:
				LoginMD5Mac loginMD5Mac = new LoginMD5Mac();
				loginMD5Mac.receive(fd, false);
				sd.setVersion(loginMD5Mac.getVersion());
				sd.setUsername(loginMD5Mac.getUsername());
				sd.setPassword(loginMD5Mac.getPassword());
				sd.setClientType(ClientType.parse(loginMD5Mac.getClientType()));
				usingRawPassword = false;
				break;
		}

		return parseRequest(usingRawPassword, sd);
	}

	/**
	 * Procedimento que ir� fazer a conclus�o da autentica��o da solicita��o de um cliente.
	 * Neste momentos os dados passados pelo cliente j� ter�o sido lidos e guardados na sess�o.
	 * @param usingRawPassword true se estiver usando senha direta ou false se for md5.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se for autenticado com �xito ou false caso contr�rio.
	 */

	private boolean parseRequest(boolean usingRawPassword, LoginSessionData sd)
	{
		if (usingRawPassword)
		{
			logNotice("solicita��o de conex�o de %s (ip: %s, version: %d)\n", sd.getUsername(), sd.getAddressString(), sd.getVersion());

			if (getConfigs().getBool("login.use_md5_password"))
				sd.setPassword(md5Encrypt(sd.getPassword()));

			sd.getPassDencrypt().setValue(0);
		}

		else
		{
			log("solicita��o de conex�o passdenc de %s (ip: %s, version: %d)\n", sd.getUsername(), sd.getAddressString(), sd.getVersion());

			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT);
			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT2);
			sd.setPassword(binToHex(sd.getPassword(), 16));
		}

		if (sd.getPassDencrypt().getValue() != 0 && getConfigs().getBool("login.use_md5_password"))
		{
			client.sendAuthResult(sd.getFileDescriptor(), AuthResult.REJECTED_FROM_SERVER);
			return false;
		}

		AuthResult result = login.authLogin(sd, false);

		if (result != AuthResult.OK)
		{
			authFailed(sd, result);
			return false;
		}

		authOk(sd);
		return true;
	}

	/**
	 * Chamado internamente sempre que uma solicita��o de acesso tiver falhado na autentica��o.
	 * Deve registrar a falha se habilitado o log e responder ao cliente qual o motivo da falha.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailed(LoginSessionData sd, AuthResult result)
	{
		authFailedLog(sd, result);
		authFailedResponse(sd, result);
	}

	/**
	 * Registra uma solicita��o de acesso que n�o foi autenticada corretamente.
	 * Esse registro � feito no banco de dados para identificar quem falhou.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailedLog(LoginSessionData sd, AuthResult result)
	{
		if (getConfigs().getBool("log.login"))
		{
			if (IntUtil.interval(result.CODE, 0, 15))
				log.add(sd.getAddress(), sd, result.CODE, loginMessage(result.CODE));

			else if (IntUtil.interval(result.CODE, 99, 104))
				log.add(sd.getAddress(), sd, result.CODE, loginMessage(result.CODE-83));

			else
				log.add(sd.getAddress(), sd, result.CODE, loginMessage(22));
		}		

		if (result.CODE == 0 || result.CODE == 1)
			ipban.addBanLog(sd.getAddressString());
	}

	/**
	 * Procedimento que ir� responder ao cliente os detalhes do resultado da autentica��o.
	 * Caso o cliente esteja banido no servidor ir� informar at� quando o mesmo ocorre.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailedResponse(LoginSessionData sd, AuthResult result)
	{
		String blockDate = "";

		if (result == AuthResult.BANNED_UNTIL)
		{
			Account account = (Account) sd.getFileDescriptor().getCache();
			Time unbanTime = account.getUnban();

			if (unbanTime != null && unbanTime.get() > 0)
				blockDate = unbanTime.toStringFormat(DATE_FORMAT);
			else
				blockDate = "! Banido !";
		}

		client.refuseLogin(sd, result, blockDate);
	}

	/**
	 * Chamado internamente sempre que uma solicita��o de acesso tiver sido aprovada na autentica��o.
	 * O segundo passo � verificar a conex�o e estado do servidor, grupos habilitados e se est� online.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 */

	private void authOk(LoginSessionData sd)
	{
		if (!authServerConnected(sd) || !authServerState(sd) || !authGroupAccount(sd) || !authIsntOnline(sd))
		{
			client.sendNotifyResult(sd.getFileDescriptor(), SERVER_CLOSED);
			return;
		}

		Login login = new Login();
		login.setID(sd.getID());
		login.setUsername(sd.getUsername());
		login.setPassword(sd.getPassword());

		log.add(new InternetProtocol(sd.getAddress()), login, 100, "login ok");

		logNotice("conex�o da conta '%s' aceita.\n", sd.getUsername());

		client.sendCharServerList(sd);

		AuthNode node = new AuthNode();
		node.setAccountID(sd.getID());
		node.setSeed(sd.getSeed());
		node.getIP().set(sd.getAddress());
		node.setVersion(sd.getVersion());
		node.setClientType(sd.getClientType());

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer waitingDisconnect = timers.acquireTimer();
		waitingDisconnect.setTick(ts.getCurrentTime() + AUTH_TIMEOUT);
		waitingDisconnect.setListener(this.login.waitingDisconnectTimer);

		OnlineLogin online = new OnlineLogin();
		online.setAccountID(sd.getID());
		online.setWaitingDisconnect(waitingDisconnect);
		online.setCharServer(OnlineLogin.NONE);
		onlines.add(online);
	}

	/**
	 * Faz a autentica��o para verificar se h� algum servidor de personagens conectado.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se houver ao menos um servidor ou false se n�o houver nenhum
	 */

	private boolean authServerConnected(LoginSessionData sd)
	{
		Account account = (Account) sd.getFileDescriptor().getCache();
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
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se estiver habilitado a conectar-se nesse servidor.
	 */

	private boolean authGroupAccount(LoginSessionData sd)
	{
		int groupToConnect = getConfigs().getInt("login.group_to_connnect");
		int minGroupToConnect = getConfigs().getInt("login.min_group_to_connect");

		if (groupToConnect == 0 && minGroupToConnect == 0)
			return true;

		Account account = (Account) sd.getFileDescriptor().getCache();

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
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se n�o houver ningu�m na conta online ou false caso contr�rio.
	 */

	private boolean authIsntOnline(LoginSessionData sd)
	{
		Account account = (Account) sd.getFileDescriptor().getCache();
		OnlineLogin online = onlines.get(account.getID());

		if (online != null)
		{
			int id = online.getCharServerID();

			CharServerList servers = getServer().getCharServerList();
			ClientCharServer server = servers.get(id);

			if (server != null)
			{
				authIsOnline(sd.getFileDescriptor(), account, online, server);
				return true;
			}

			controller.remove(sd.getID());
			onlines.remove(online);
		}

		return true;
	}

	/**
	 * A autentica��o do cliente indicou que a conta j� est� sendo usada (online).
	 * Notificar ao cliente de que a conta que o servidor ainda o considera online.
	 * @param fd refer�ncia da conex�o com o cliente para receber e enviar dados.
	 * @param account objeto contendo os detalhes da conta que est� sendo acessada.
	 * @param online objeto que cont�m o gatilho para efetuar logout for�ado.
	 * @param server servidor de personagens do qual a conta est� online.
	 */

	private void authIsOnline(FileDescriptor fd, Account account, OnlineLogin online, ClientCharServer server)
	{
		logNotice("usu�rio '%s' j� est� online em '%s'.\n", account.getUsername(), server.getName());

		AlreadyOnline packet = new AlreadyOnline();
		packet.setAccountID(account.getID());

		client.sendAllWithoutOurSelf(packet);

		if (online.getWaitingDisconnect().getType() == TIMER_INVALID)
		{
			TimerSystem ts = getTimerSystem();

			Timer timer = online.getWaitingDisconnect();
			timer.setListener(login.waitingDisconnectTimer);
			timer.setTick(ts.getCurrentTime());
			ts.getTimers().add(timer);

			client.sendNotifyResult(fd, RECOGNIZES_LAST_LOGIN);

			return;
		}		
	}

	/**
	 * Chamado quando um servidor de personagens solicita a conex�o com o servidor de acesso.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se tiver sido autorizado ou false caso contr�rio.
	 */

	private boolean requestCharConnect(FileDescriptor fd, LoginSessionData sd)
	{
		CharConnectReceive ccPacket = new CharConnectReceive();
		ccPacket.receive(fd, false);

		sd.setUsername(ccPacket.getUsername());
		sd.setPassword(ccPacket.getPassword());

		if (getConfigs().getBool("login.user_md5_password"))
			sd.setPassword(md5Encrypt(sd.getPassword()));

		sd.getPassDencrypt().setValue(0);
		sd.setVersion(getConfigs().getInt("client.version"));

		String serverName = ccPacket.getServerName();
		int serverIP = ccPacket.getServerIP();
		short serverPort = ccPacket.getServerPort();
		short type = ccPacket.getType();
		short newDisplay = ccPacket.getNewDisplay();

		logInfo("conex�o solicitada do servidor de personagens %s@%s (account: %s, pass: %s).\n", serverName, sd.getAddressString(), sd.getUsername(), sd.getPassword());

		String message = format("charserver - %s@%s:%d", serverName, SocketUtil.socketIP(serverIP), serverPort);
		log.add(fd.getAddress(), sd, 100, message);

		AuthResult result = login.authLogin(sd, true);

		if (getServer().isState(ServerState.RUNNING) && result == AuthResult.OK && fd.isConnected())
		{
			log("conex�o do servidor de personagens '%s' aceita.\n", serverName);

			ClientCharServer server = new ClientCharServer(fd);
			server.setFileDecriptor(fd);
			server.setName(serverName);
			server.setIP(new InternetProtocol(serverIP));
			server.setPort(serverPort);
			server.setUsers((short) 0);
			server.setType(CharServerType.parse(type));
			server.setNewDisplay(newDisplay);
			getServer().getCharServerList().add(server);

			fd.setParseListener(character.parse);
			fd.getFlag().set(FileDescriptor.FLAG_SERVER);

			client.charServerResult(fd, OK);
			return true;
		}

		logNotice("Conex�o com o servidor de personagens '%s' RECUSADA.\n", serverName);

		client.charServerResult(fd, REJECTED_FROM_SERVER);
		return false;
	}
}

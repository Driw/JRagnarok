package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokConstants.DATE_FORMAT;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.binToHex;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.loginMessage;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Encrypt;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN2;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN3;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN4;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN_HAN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN_PCBANG;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_SSO_LOGIN_REQ;
import static org.diverproject.jragnarok.packets.common.NotifyAuthResult.RECOGNIZES_LAST_LOGIN;
import static org.diverproject.jragnarok.packets.common.NotifyAuthResult.SERVER_CLOSED;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.OK;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.REJECTED_FROM_SERVER;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnarok.packets.common.RefuseLogin;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_CharServerConnect;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AlreadyOnline;
import org.diverproject.jragnarok.packets.login.fromclient.CA_Login;
import org.diverproject.jragnarok.packets.login.fromclient.CA_Login2;
import org.diverproject.jragnarok.packets.login.fromclient.CA_Login3;
import org.diverproject.jragnarok.packets.login.fromclient.CA_Login4;
import org.diverproject.jragnarok.packets.login.fromclient.CA_LoginHan;
import org.diverproject.jragnarok.packets.login.fromclient.CA_LoginPCBang;
import org.diverproject.jragnarok.packets.login.fromclient.CA_LoginSingleSignOn;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.ServerState;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.common.CharServerType;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.Time;
import org.diverproject.util.lang.IntUtil;

/**
 * <h1>Serviço para Autenticar Acesso</h1>
 *
 * <p>Esse serviço deverá ser responsável pela autenticação dos dados recebidos de um cliente.
 * Fará o contato com o banco de dados solicitando os dados para realizar a devida autenticação.
 * Caso haja problemas durante a autenticação deverá repassar ao cliente os problemas encontrados.
 * Também fica de sua conta reconhecer o tipo de acesso que o cliente está solicitando.</p>
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
	 * Tempo para que uma autenticação entre em timeout.
	 */
	private static final int AUTH_TIMEOUT = seconds(10);


	/**
	 * Serviço para comunicação entre o servidor e o cliente.
	 */
	private ServiceLoginClient client;

	/**
	 * Controle para persistência das contas de jogadores.
	 */
	private AccountControl accounts;

	/**
	 * Serviço para acesso de contas (serviço principal)
	 */
	private ServiceLoginServer login;

	/**
	 * Serviço para banimento de acessos por endereço de IP.
	 */
	private ServiceLoginIpBan ipban;

	/**
	 * Serviço para registro de acessos.
	 */
	private ServiceLoginLog log;

	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineMap onlines;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthAccountMap auths;

	/**
	 * Cria um novo serviço para autenticação de solicitações dos acessos ao servidor.
	 * Esse serviço fará o contato com o banco de dados e autenticações necessárias.
	 * @param server referência do servidor de acesso que deseja criar o serviço.
	 */

	public ServiceLoginAuth(LoginServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getClientService();
		accounts = getServer().getFacade().getAccountControl();
		ipban = getServer().getFacade().getIpBanService();
		log = getServer().getFacade().getLogService();
		login = getServer().getFacade().getLoginService();
		onlines = getServer().getFacade().getOnlineMap();
		auths = getServer().getFacade().getAuthAccountMap();
	}

	@Override
	public void destroy()
	{
		client = null;
		accounts = null;
		ipban = null;
		log = null;
		login = null;
		onlines = null;
		auths = null;
	}

	/**
	 * Efetua a solicitação de acesso com o servidor de personagens recebido de um cliente.
	 * Para este caso o cliente já é reconhecido como um jogador através do executável.
	 * Deverá receber os dados do cliente adequadamente conforme o tipo de autenticação.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param command qual o comando que foi executado (tipo de pacote).
	 * @return true se efetuar a análise com êxito ou false caso contrário.
	 */

	public boolean requestAuth(LFileDescriptor fd, short command)
	{
		boolean usingRawPassword = true;
		LoginSessionData sd = fd.getSessionData();

		switch (command)
		{
			case PACKET_CA_LOGIN:
				CA_Login loginPacket = new CA_Login();
				loginPacket.receive(fd);
				sd.setVersion(loginPacket.getVersion());
				sd.setClientType(loginPacket.getClientType());
				sd.setUsername(loginPacket.getUsername());
				sd.setPassword(loginPacket.getPassword());
				break;

			case PACKET_CA_LOGIN_PCBANG:
				CA_LoginPCBang loginPCBang = new CA_LoginPCBang();
				loginPCBang.receive(fd);
				sd.setVersion(loginPCBang.getVersion());
				sd.setClientType(loginPCBang.getClientType());
				sd.setUsername(loginPCBang.getUsername());
				sd.setPassword(loginPCBang.getPassword());
				break;

			case PACKET_CA_LOGIN_HAN:
				CA_LoginHan loginHan = new CA_LoginHan();
				loginHan.receive(fd);
				sd.setVersion(loginHan.getVersion());
				sd.setClientType(loginHan.getClientType());
				sd.setUsername(loginHan.getUsername());
				sd.setPassword(loginHan.getPassword());
				break;

			case PACKET_CA_SSO_LOGIN_REQ:
				CA_LoginSingleSignOn loginSingleSignOn = new CA_LoginSingleSignOn();
				loginSingleSignOn.receive(fd);
				sd.setVersion(loginSingleSignOn.getVersion());
				sd.setClientType(loginSingleSignOn.getClientType());
				sd.setUsername(loginSingleSignOn.getUsername());
				sd.setPassword(loginSingleSignOn.getToken());
				break;

			case PACKET_CA_LOGIN2:
				CA_Login2 loginMD5 = new CA_Login2();
				loginMD5.receive(fd);
				sd.setVersion(loginMD5.getVersion());
				sd.setUsername(loginMD5.getUsername());
				sd.setPassword(loginMD5.getPassword());
				sd.setClientType(loginMD5.getClientType());
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN3:
				CA_Login3 loginMD5Info = new CA_Login3();
				loginMD5Info.receive(fd);
				sd.setVersion(loginMD5Info.getVersion());
				sd.setUsername(loginMD5Info.getUsername());
				sd.setPassword(loginMD5Info.getPassword());
				sd.setClientType(loginMD5Info.getClientType());
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN4:
				CA_Login4 loginMD5Mac = new CA_Login4();
				loginMD5Mac.receive(fd);
				sd.setVersion(loginMD5Mac.getVersion());
				sd.setUsername(loginMD5Mac.getUsername());
				sd.setPassword(loginMD5Mac.getPassword());
				sd.setClientType(loginMD5Mac.getClientType());
				usingRawPassword = false;
				break;
		}

		return parseRequest(fd, usingRawPassword);
	}

	/**
	 * Procedimento que irá fazer a conclusão da autenticação da solicitação de um cliente.
	 * Neste momentos os dados passados pelo cliente já terão sido lidos e guardados na sessão.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param usingRawPassword true se estiver usando senha direta ou false se for md5.
	 * @return true se for autenticado com êxito ou false caso contrário.
	 */

	private boolean parseRequest(LFileDescriptor fd, boolean usingRawPassword)
	{
		LoginSessionData sd = fd.getSessionData();

		if (usingRawPassword)
		{
			logNotice("solicitação de conexão de %s (ip: %s, version: %d)\n", sd.getUsername(), fd.getAddressString(), sd.getVersion());

			if (getConfigs().getBool("login.use_md5_password"))
				sd.setPassword(md5Encrypt(sd.getPassword()));

			sd.getPassDencrypt().setValue(b(0));
		}

		else
		{
			log("solicitação de conexão passdenc de %s (ip: %s, version: %d)\n", sd.getUsername(), fd.getAddressString(), sd.getVersion());

			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT);
			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT2);
			sd.setPassword(binToHex(sd.getPassword(), 16));
		}

		if (sd.getPassDencrypt().getValue() != 0 && getConfigs().getBool("login.use_md5_password"))
		{
			client.sendAuthResult(fd, RefuseLogin.REJECTED_FROM_SERVER);
			return false;
		}

		RefuseLogin result = login.parseAuthLogin(fd, false);

		if (result != RefuseLogin.OK)
		{
			authFailed(fd, result);
			return false;
		}

		authOk(fd);
		return true;
	}

	/**
	 * Chamado internamente sempre que uma solicitação de acesso tiver falhado na autenticação.
	 * Deve registrar a falha se habilitado o log e responder ao cliente qual o motivo da falha.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultando obtido da autenticação feita com o cliente.
	 */

	private void authFailed(LFileDescriptor fd, RefuseLogin result)
	{
		authFailedLog(fd, result);
		authFailedResponse(fd, result);
	}

	/**
	 * Registra uma solicitação de acesso que não foi autenticada corretamente.
	 * Esse registro é feito no banco de dados para identificar quem falhou.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultando obtido da autenticação feita com o cliente.
	 */

	private void authFailedLog(LFileDescriptor fd, RefuseLogin result)
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
	 * Procedimento que irá responder ao cliente os detalhes do resultado da autenticação.
	 * Caso o cliente esteja banido no servidor irá informar até quando o mesmo ocorre.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultando obtido da autenticação feita com o cliente.
	 */

	private void authFailedResponse(LFileDescriptor fd, RefuseLogin result)
	{
		String blockDate = "";
		LoginSessionData sd = fd.getSessionData();

		if (result == RefuseLogin.BANNED_UNTIL)
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
	 * Chamado internamente sempre que uma solicitação de acesso tiver sido aprovada na autenticação.
	 * O segundo passo é verificar a conexão e estado do servidor, grupos habilitados e se está online.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	private void authOk(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();

		if (!authServerConnected(fd) || !authServerState(sd) || !authGroupAccount(fd))
		{
			client.sendNotifyResult(fd, SERVER_CLOSED);
			return;
		}

		if (!authIsntOnline(fd))
		{
			client.sendNotifyResult(fd, RECOGNIZES_LAST_LOGIN);
			return;
		}

		Account account = (Account) sd.getCache();

		log.add(new InternetProtocol(fd.getAddress()), account, 100, "login ok");

		logNotice("conexão da conta '%s' aceita.\n", sd.getUsername());

		AuthNode node = new AuthNode();
		node.setAccountID(sd.getID());
		node.getSeed().copyFrom(sd.getSeed());
		node.getIP().set(fd.getAddress());
		node.setVersion(sd.getVersion());
		node.setClientType(sd.getClientType());
		node.setSex(sd.getSex());
		auths.add(node);

		client.sendCharServerList(fd);
		login.addOnlineUser(OnlineLogin.NO_CHAR_SERVER, sd.getID());
	}

	/**
	 * Faz a autenticação para verificar se há algum servidor de personagens conectado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se houver ao menos um servidor ou false se não houver nenhum
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
	 * Faz a autenticação do estado do servidor com o qual está tentando se conectar.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @return true se estiver rodando ou false caso contrário.
	 */

	private boolean authServerState(LoginSessionData sd)
	{
		boolean ok = getServer().getState() == RUNNING;

		if (!ok)
			logNotice("servidor não está rodando (state: %s, usernme: %s).\n", getServer().getState(), sd.getUsername());

		return ok;
	}

	/**
	 * Faz a autenticação para verificar se o cliente está contido nos grupos habilitados.
	 * Essa autenticação só será válida caso tenha sido configurado grupos de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * Autentica uma conta em relação ao único grupo de contas habilitado a conectar.
	 * @param account referência da conta do cliente que deverá ser autenticada.
	 * @param required nível da conta necessária para se contar.
	 * @return true se estiver habilitado ou false caso contrário.
	 */

	private boolean authGroupToConnect(Account account, int required)
	{
		int group = account.getGroup().getCurrentGroup().getID();
		String username = account.getUsername();

		if (required == 0 || group == required)
			return true;

		logNotice("grupo '%d' é o único aceito (username: %s, grupoid: %d).\n", required, username, group);

		return false;
	}

	/**
	 * Autentica uma conta para ver se possui nível de grupo maior do mínimo aceito.
	 * Essa verificação só será válida caso o grupo requerido seja igual a -1.
	 * @param account referência da conta do cliente que deverá ser autenticada.
	 * @param required nível do grupo para se conectar ao servidor (esperado -1).
	 * @param min nível do grupo mínimo aceito para se conectar no servidor.
	 * @return true se tiver um grupo habilitado ou false caso contrário.
	 */

	private boolean authMinGroupToConnect(Account account, int required, int min)
	{
		int group = account.getGroup().getCurrentGroup().getID();
		String username = account.getUsername();

		if (required == -1 && min < group)
			return true;

		logNotice("grupo '%d' é o mínimo aceito (usernme: %s, groupid: %d).\n", min, username, group);

		return false;
	}

	/**
	 * Autentica um cliente verificando se há um outro cliente usando a conta acessada.
	 * Caso haja um cliente usado a conta, deverá avisar quem está online e rejeitar o acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se não houver ninguém na conta online ou false caso contrário.
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
				return false;
			}

			auths.remove(sd.getID());
			login.removeOnlineUser(online.getAccountID());
		}

		return true;
	}

	/**
	 * A autenticação do cliente indicou que a conta já está sendo usada (online).
	 * Notificar ao cliente de que a conta que o servidor ainda o considera online.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os detalhes da conta que está sendo acessada.
	 * @param online objeto que contém o gatilho para efetuar logout forçado.
	 * @param server servidor de personagens do qual a conta está online.
	 */

	private void authIsOnline(LFileDescriptor fd, Account account, OnlineLogin online, ClientCharServer server)
	{
		logNotice("usuário '%s' já está online em '%s'.\n", account.getUsername(), server.getName());

		AH_AlreadyOnline packet = new AH_AlreadyOnline();
		packet.setAccountID(account.getID());

		client.sendAllWithoutOurSelf(fd, packet);

		if (online.getWaitingDisconnect() == null)
		{
			TimerSystem ts = getTimerSystem();
			TimerMap timers = ts.getTimers();

			Timer timer = timers.acquireTimer();
			timer.setTick(ts.getCurrentTime());
			timer.setObjectID(account.getID());
			timer.setListener(WAITING_DISCONNECT_TIMER);
			timers.addInterval(timer, AUTH_TIMEOUT);
		}
	}

	/**
	 * Função para temporizadores executarem a remoção de uma conta como acesso online.
	 */

	public final TimerListener WAITING_DISCONNECT_TIMER = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			int accountID = timer.getObjectID();
			OnlineLogin online = onlines.get(accountID);

			if (online == null)
				getTimerSystem().getTimers().delete(timer);
			else
			{
				onlines.remove(accountID);

				if (online.getWaitingDisconnect() != null)
				{
					getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
					online.setWaitingDisconnect(null);
				}
			}
		}

		@Override
		public String getName()
		{
			return "waitingDisconnectTimer";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	/**
	 * Chamado quando um servidor de personagens solicita a conexão com o servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @return true se tiver sido autorizado ou false caso contrário.
	 */

	public boolean requestCharConnect(LFileDescriptor fd)
	{
		HA_CharServerConnect packet = new HA_CharServerConnect();
		packet.receive(fd);

		Account account = accounts.get(packet.getUsername());

		if (account == null)
		{
			logWarning("char-server com usuário não encontrado (name: %s, username: %s).\n", packet.getServerName(), packet.getUsername());
			return false;
		}

		if (!account.getPassword().equals(packet.getPassword()))
		{
			logWarning("char-server com senha incompatível (name: %s, username: %s).\n", packet.getServerName(), packet.getUsername());
			return false;
		}
		
		LoginSessionData sd = fd.getSessionData();
		sd.setID(account.getID());
		sd.setUsername(packet.getUsername());
		sd.setPassword(packet.getPassword());
		sd.setGroup(account.getGroup().getCurrentGroup());

		if (getConfigs().getBool("login.user_md5_password"))
			sd.setPassword(md5Encrypt(sd.getPassword()));

		sd.getPassDencrypt().setValue(b(0));
		sd.setVersion(getConfigs().getInt("client.version"));

		String serverName = packet.getServerName();
		int serverIP = packet.getServerIP();
		short serverPort = packet.getServerPort();
		short type = packet.getType();
		boolean newDisplay = packet.getNewDisplay();

		logInfo("conexão solicitada do servidor de personagens %s@%s (account: %s, pass: %s).\n", serverName, fd.getAddressString(), sd.getUsername(), sd.getPassword());

		String message = format("charserver - %s@%s:%d", serverName, SocketUtil.socketIP(serverIP), serverPort);
		log.add(fd.getAddress(), sd, 100, message);

		RefuseLogin result = login.parseAuthLogin(fd, true);

		if (getServer().isState(ServerState.RUNNING) && result == RefuseLogin.OK && fd.isConnected())
		{
			logNotice("conexão do servidor de personagens '%s' aceita.\n", serverName);

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
			client.sendGroupData(fd, getServer().getFacade().getGroupControl());

			return true;
		}

		logNotice("Conexão com o servidor de personagens '%s' RECUSADA.\n", serverName);

		client.sendCharServerResult(fd, REJECTED_FROM_SERVER);
		return false;
	}
}

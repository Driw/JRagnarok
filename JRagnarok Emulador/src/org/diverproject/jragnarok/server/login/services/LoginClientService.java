package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.jragnarok.JRagnarokConstants.DATE_FORMAT;
import static org.diverproject.jragnarok.JRagnarokUtil.binToHex;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.loginMessage;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Encrypt;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Salt;
import static org.diverproject.jragnarok.JRagnarokUtil.random;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.JRagnarokUtil.skip;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_CONNECT_INFO_CHANGED;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_EXE_HASHCHECK;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN2;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN3;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN4;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN_HAN;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN_PCBANG;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_REQ_CHAR_CONNECT;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_REQ_HASH;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_SSO_LOGIN_REQ;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.login.structures.NotifyAuthResult.RECOGNIZES_LAST_LOGIN;
import static org.diverproject.jragnarok.server.login.structures.NotifyAuthResult.SERVER_CLOSED;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.AcknologeHash;
import org.diverproject.jragnarok.packets.AlreadyOnline;
import org.diverproject.jragnarok.packets.CharConnectReceive;
import org.diverproject.jragnarok.packets.KeepAlivePacket;
import org.diverproject.jragnarok.packets.ListCharServers;
import org.diverproject.jragnarok.packets.LoginHan;
import org.diverproject.jragnarok.packets.LoginMD5;
import org.diverproject.jragnarok.packets.LoginMD5Info;
import org.diverproject.jragnarok.packets.LoginMD5Mac;
import org.diverproject.jragnarok.packets.LoginPCBang;
import org.diverproject.jragnarok.packets.LoginPacket;
import org.diverproject.jragnarok.packets.LoginSingleSignOn;
import org.diverproject.jragnarok.packets.NotifyAuth;
import org.diverproject.jragnarok.packets.ReceivePacketIDPacket;
import org.diverproject.jragnarok.packets.RefuseLoginBytePacket;
import org.diverproject.jragnarok.packets.RefuseLoginIntPacket;
import org.diverproject.jragnarok.packets.CharConnectResult;
import org.diverproject.jragnarok.packets.UpdateClientHashPacket;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorAction;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.ServerState;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.LoginCharServers;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.login.controllers.AuthController;
import org.diverproject.jragnarok.server.login.controllers.OnlineController;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AuthNode;
import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.jragnarok.server.login.entities.OnlineLogin;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.jragnarok.server.login.structures.CharServerType;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.jragnarok.server.login.structures.ClientHash;
import org.diverproject.jragnarok.server.login.structures.ClientType;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;
import org.diverproject.jragnarok.server.login.structures.NotifyAuthResult;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.Time;
import org.diverproject.util.lang.HexUtil;
import org.diverproject.util.lang.IntUtil;

/**
 * Serviço para Acesso dos Clientes
 *
 * Esse serviço é a primeira fronteira do servidor para se comunicar com o cliente.
 * Nele será recebido uma nova conexão solicitada para com o servidor de acesso.
 * Após receber a conexão deverá analisar e verificar qual o tipo de acesso solicitado.
 *
 * De acordo com o tipo de acesso solicitado, deverá redirecionar a outros serviços se necessário.
 * Fica sendo de sua responsabilidade garantir a autenticação de qualquer tipo de acesso.
 * Podendo ainda ser necessário comunicar-se com outro serviço para auxiliá-lo.
 *
 * @see LoginService
 * @see LoginLogService
 * @see LoginIpBanService
 * @see LoginCharacterService
 * @see OnlineController
 * @see AuthController
 *
 * @author Andrew Mello
 */

public class LoginClientService extends LoginServerService
{
	/**
	 * Tempo para que uma autenticação entre em timeout.
	 */
	private static final int AUTH_TIMEOUT = seconds(30);

	/**
	 * Serviço de acesso de contas.
	 */
	private LoginService login;

	/**
	 * Serviço para registro dos acessos.
	 */
	private LoginLogService log;

	/**
	 * Serviço para banimento por endereço de IP.
	 */
	private LoginIpBanService ipban;

	/**
	 * Serviço para tratar dos servidores de personagens.
	 */
	private LoginCharacterService character;

	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineController onlines;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthController auths;

	/**
	 * Cria um novo serviço de recebimento dos novos clientes com o servidor.
	 * Esse serviço é voltado para o servidor de acesso, para tal defini-lo.
	 * @param server referência do servidor de acesso que deseja criar o serviço.
	 */

	public LoginClientService(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa o serviço para recebimento de novos clientes no servidor.
	 */

	public void init()
	{
		log = getServer().getLogService();
		ipban = getServer().getIpBanService();
		login = getServer().getLoginService();
		character = getServer().getCharService();
		onlines = new OnlineController(this);
	}

	/**
	 * Listener usado para receber novas conexões solicitadas com o servidor de acesso.
	 * A análise verifica se a conexão já foi feita e se tiver verifica se está banido.
	 * Caso não esteja banido ou não haja conexão estabelece uma nova conexão.
	 */

	public FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logInfo("Conexão fechada (ip: %s).\n", fd.getAddressString());
				return false;
			}

			// Já conectou, verificar se está banido
			if (fd.getCache() == null)
				if (!parseBanTime(fd))
					return true;

			LoginSessionData sd = new LoginSessionData(fd);

			ReceivePacketIDPacket packetReceivePacketID = new ReceivePacketIDPacket();
			packetReceivePacketID.receive(fd);

			short command = packetReceivePacketID.getPacketID();

			return dispatch(command, fd, sd);
		}
	};

	/**
	 * Após a análise primária da conexão é feito o despachamento do mesmo.
	 * Esse despache considera o tipo de pacote que foi recebido do cliente.
	 * O pacote indica qual o tipo de informação usada para se conectar.
	 * @param command código do pacote que foi recebido do cliente.
	 * @param fd referência da conexão com o cliente para enviar e receber dados.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @return true se despachar corretamente ou false caso contrário.
	 */

	private boolean dispatch(short command, FileDescriptor fd, LoginSessionData sd)
	{
		switch (command)
		{
			case PACKET_CA_CONNECT_INFO_CHANGED:
				keepAlive(fd);
				break;

			case PACKET_CA_EXE_HASHCHECK:
				updateClientHash(fd, sd);
				break;

			case PACKET_CA_REQ_HASH:
				parseRequestKey(fd, sd);
				break;

			// Solicitação de acesso com senha direta
			case PACKET_CA_LOGIN:
			case PACKET_CA_LOGIN_PCBANG:
			case PACKET_CA_LOGIN_HAN:
			case PACKET_CA_SSO_LOGIN_REQ:
			// Solicitação de acesso com senha MD5
			case PACKET_CA_LOGIN2:
			case PACKET_CA_LOGIN3:
			case PACKET_CA_LOGIN4:
				return requestAuth(fd, sd, command);

			case PACKET_CA_REQ_CHAR_CONNECT:
				requestCharConnect(fd, sd);
				return true;

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logNotice("fim de conexão inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}

		return true;
	};

	/**
	 * Verifica se o endereço de IP de uma conexão foi banida afim de recusar seu acesso.
	 * Essa operação só terá efeito se tiver sido habilitado o banimento por IP.
	 * @param fd referência da conexão do qual deseja verificar o banimento.
	 * @return true se estiver liberado o acesso ou false se estiver banido.
	 */

	private boolean parseBanTime(FileDescriptor fd)
	{
		if (getConfigs().getBool("ipban.enabled") && ipban.isBanned(fd.getAddressString()))
		{
			log("conexão recusada, ip não autorizado (ip: %s).\n", fd.getAddressString());

			log.addLoginLog(fd.getAddress(), null, -3, "ip banned");
			skip(fd, false, 23);

			RefuseLoginBytePacket refuseLoginPacket = new RefuseLoginBytePacket();
			refuseLoginPacket.setResult(AuthResult.REJECTED_FROM_SERVER);
			refuseLoginPacket.setBlockDate("");
			refuseLoginPacket.send(fd);

			fd.close();

			return false;
		}

		return true;
	}

	/**
	 * Envia um pacote para manter a conexão com o jogador, assim é possível evitar timeout.
	 * Quando uma conexão para de transmitir ou receber dados irá dar timeout no mesmo.
	 * Se a conexão chegar em timeout significa que o mesmo deverá ser fechado.
	 * @param fd referência do objeto contendo a conexão do cliente.
	 */

	private void keepAlive(FileDescriptor fd)
	{
		KeepAlivePacket keepAlivePacket = new KeepAlivePacket();
		keepAlivePacket.receive(fd);
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd referência da conexão com o cliente que será recebido.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 */

	private void updateClientHash(FileDescriptor fd, LoginSessionData sd)
	{
		UpdateClientHashPacket updateClientHashPacket = new UpdateClientHashPacket();
		updateClientHashPacket.receive(fd);

		sd.setClientHash(new ClientHash());
		sd.getClientHash().set(updateClientHashPacket.getHashValue());
	}

	/**
	 * Envia o resultado de uma autenticação de conexão com o servidor de acesso.
	 * @param fd referência da conexão com o cliente que será enviado.
	 * @param result resultado da autenticação solicitada pelo cliente.
	 */

	private void sendAuthResult(FileDescriptor fd, AuthResult result)
	{
		RefuseLoginBytePacket refuseLoginPacket = new RefuseLoginBytePacket();
		refuseLoginPacket.setResult(result);
		refuseLoginPacket.send(fd);
	}

	/**
	 * Notifica o cliente que houve algum problema após a autenticação do acesso.
	 * O acesso foi autenticado porém houve algum problema em liberar o acesso.
	 * @param fd referência da conexão com o cliente que será enviado.
	 * @param result resultado da liberação do acesso para o cliente.
	 */

	private void sendNotifyResult(FileDescriptor fd, NotifyAuthResult result)
	{
		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia os dados de um mesmo pacote para todos os clientes conectados com o servidor.
	 * A ação é feita por um listener (FileDecriptorAction) adicionado em FileDecriptor.
	 * @param packet referência do pacote contendo os dados a serem enviados.
	 */

	private void sendAllWithoutOurSelf(ResponsePacket packet)
	{
		getFileDescriptorSystem().execute(new FileDescriptorAction()
		{
			@Override
			public void execute(FileDescriptor fd)
			{
				packet.send(fd);
			}
		});
	}

	/**
	 * Analisa uma solicitação de um cliente para gerar uma chave de acesso com o servidor.
	 * Deve gerar a chave e enviar o mesmo para o cliente possuir a chave de acesso.
	 * @param fd referência da conexão com o cliente que será enviado.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 */

	private void parseRequestKey(FileDescriptor fd, LoginSessionData sd)
	{
		short md5KeyLength = (short) (12 + (random() % 4));
		String md5Key = md5Salt(md5KeyLength);

		sd.setMd5Key(md5Key);
		sd.setMd5KeyLenght(md5KeyLength);

		AcknologeHash packet = new AcknologeHash();
		packet.setMD5KeyLength(md5KeyLength);
		packet.setMD5Key(md5Key);
		packet.send(fd);
	}

	/**
	 * Envia ao cliente uma lista contendo os dados dos servidores de personagens.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 */

	private void sendCharServerList(LoginSessionData sd)
	{
		LoginCharServers servers = getServer().getCharServers();

		ListCharServers packet = new ListCharServers();
		packet.setServers(servers);
		packet.setSessionData(sd);
		packet.send(sd.getFileDescriptor());
	}

	/**
	 * Após a primeira análise verifica qual o tipo de acesso (pacote) que o cliente está enviado.
	 * Esse pacote deverá representar uma forma de acesso e cada uma possui seu tipo de informação.
	 * @param fd referência da conexão com o cliente para receber e enviar dados.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @param command qual o comando que foi executado (tipo de pacote).
	 * @return true se efetuar a análise com êxito ou false caso contrário.
	 */

	private boolean requestAuth(FileDescriptor fd, LoginSessionData sd, short command)
	{
		boolean usingRawPassword = true;

		switch (command)
		{
			case PACKET_CA_LOGIN:
				LoginPacket loginPacket = new LoginPacket();
				loginPacket.receive(fd);
				sd.setVersion(loginPacket.getVersion());
				sd.setClientType(ClientType.parse(loginPacket.getClientType()));
				sd.setUsername(loginPacket.getUsername());
				sd.setPassword(loginPacket.getPassword());
				break;

			case PACKET_CA_LOGIN_PCBANG:
				LoginPCBang loginPCBang = new LoginPCBang();
				loginPCBang.receive(fd);
				sd.setVersion(loginPCBang.getVersion());
				sd.setClientType(ClientType.parse(loginPCBang.getClientType()));
				sd.setUsername(loginPCBang.getUsername());
				sd.setPassword(loginPCBang.getPassword());
				break;

			case PACKET_CA_LOGIN_HAN:
				LoginHan loginHan = new LoginHan();
				loginHan.receive(fd);
				sd.setVersion(loginHan.getVersion());
				sd.setClientType(ClientType.parse(loginHan.getClientType()));
				sd.setUsername(loginHan.getUsername());
				sd.setPassword(loginHan.getPassword());
				break;

			case PACKET_CA_SSO_LOGIN_REQ:
				LoginSingleSignOn loginSingleSignOn = new LoginSingleSignOn();
				loginSingleSignOn.receive(fd);
				sd.setVersion(loginSingleSignOn.getVersion());
				sd.setClientType(ClientType.parse(loginSingleSignOn.getClientType()));
				sd.setUsername(loginSingleSignOn.getUsername());
				sd.setPassword(loginSingleSignOn.getToken());
				break;

			case PACKET_CA_LOGIN2:
				LoginMD5 loginMD5 = new LoginMD5();
				loginMD5.receive(fd);
				sd.setVersion(loginMD5.getVersion());
				sd.setUsername(loginMD5.getUsername());
				sd.setPassword(loginMD5.getPassword());
				sd.setClientType(ClientType.parse(loginMD5.getClientType()));
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN3:
				LoginMD5Info loginMD5Info = new LoginMD5Info();
				loginMD5Info.receive(fd);
				sd.setVersion(loginMD5Info.getVersion());
				sd.setUsername(loginMD5Info.getUsername());
				sd.setPassword(loginMD5Info.getPassword());
				sd.setClientType(ClientType.parse(loginMD5Info.getClientType()));
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN4:
				LoginMD5Mac loginMD5Mac = new LoginMD5Mac();
				loginMD5Mac.receive(fd);
				sd.setVersion(loginMD5Mac.getVersion());
				sd.setUsername(loginMD5Mac.getUsername());
				sd.setPassword(loginMD5Mac.getPassword());
				sd.setClientType(ClientType.parse(loginMD5Mac.getClientType()));
				usingRawPassword = false;
				break;
		}

		return requestAuthConclude(usingRawPassword, sd);
	}

	/**
	 * Procedimento que irá fazer a conclusão da autenticação da solicitação de um cliente.
	 * Neste momentos os dados passados pelo cliente já terão sido lidos e guardados na sessão.
	 * @param usingRawPassword true se estiver usando senha direta ou false se for md5.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @return true se for autenticado com êxito ou false caso contrário.
	 */

	private boolean requestAuthConclude(boolean usingRawPassword, LoginSessionData sd)
	{
		if (usingRawPassword)
		{
			logNotice("solicitação de conexão de %s (ip: %s, version: %d)\n", sd.getUsername(), sd.getAddressString(), sd.getVersion());

			if (getConfigs().getBool("login.use_md5_password"))
				sd.setPassword(md5Encrypt(sd.getPassword()));

			sd.getPassDencrypt().setValue(0);
		}

		else
		{
			log("solicitação de conexão passdenc de %s (ip: %s, version: %d)\n", sd.getUsername(), sd.getAddressString(), sd.getVersion());

			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT);
			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT2);
			sd.setPassword(binToHex(sd.getPassword(), 16));
		}

		if (sd.getPassDencrypt().getValue() != 0 && getConfigs().getBool("login.use_md5_password"))
		{
			sendAuthResult(sd.getFileDescriptor(), AuthResult.REJECTED_FROM_SERVER);
			return false;
		}

		AuthResult result = login.authLogin(sd, false);

		if (result == AuthResult.OK)
		{
			authOk(sd);
			return true;
		}

		authFailed(sd, result);
		return false;
	}

	/**
	 * Chamado internamente sempre que uma solicitação de acesso tiver falhado na autenticação.
	 * Deve registrar a falha se habilitado o log e responder ao cliente qual o motivo da falha.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @param result resultando obtido da autenticação feita com o cliente.
	 */

	private void authFailed(LoginSessionData sd, AuthResult result)
	{
		authFailedLog(sd, result);
		authFailedResponse(sd, result);
	}

	/**
	 * Registra uma solicitação de acesso que não foi autenticada corretamente.
	 * Esse registro é feito no banco de dados para identificar quem falhou.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @param result resultando obtido da autenticação feita com o cliente.
	 */

	private void authFailedLog(LoginSessionData sd, AuthResult result)
	{
		if (getConfigs().getBool("log.login"))
		{
			if (IntUtil.interval(result.CODE, 0, 15))
				log.addLoginLog(sd.getAddress(), sd, result.CODE, loginMessage(result.CODE));

			else if (IntUtil.interval(result.CODE, 99, 104))
				log.addLoginLog(sd.getAddress(), sd, result.CODE, loginMessage(result.CODE-83));

			else
				log.addLoginLog(sd.getAddress(), sd, result.CODE, loginMessage(22));
		}		

		if (result.CODE == 0 || result.CODE == 1)
			ipban.addBanLog(sd.getAddressString());
	}

	/**
	 * Procedimento que irá responder ao cliente os detalhes do resultado da autenticação.
	 * Caso o cliente esteja banido no servidor irá informar até quando o mesmo ocorre.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @param result resultando obtido da autenticação feita com o cliente.
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

		if (sd.getVersion() >= dateToVersion(20120000))
		{
			RefuseLoginIntPacket packet = new RefuseLoginIntPacket();
			packet.setBlockDate(blockDate);
			packet.setCode(result);
			packet.send(sd.getFileDescriptor());
		}

		else
		{
			RefuseLoginBytePacket packet = new RefuseLoginBytePacket();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(sd.getFileDescriptor());
		}
	}

	/**
	 * Chamado internamente sempre que uma solicitação de acesso tiver sido aprovada na autenticação.
	 * O segundo passo é verificar a conexão e estado do servidor, grupos habilitados e se está online.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 */

	private void authOk(LoginSessionData sd)
	{
		if (!authServerConnected(sd) || !authServerState(sd) || !authGroupAccount(sd) || !authIsntOnline(sd))
		{
			sendNotifyResult(sd.getFileDescriptor(), SERVER_CLOSED);
			return;
		}

		Login login = new Login();
		login.setID(sd.getID());
		login.setUsername(sd.getUsername());
		login.setPassword(sd.getPassword());

		log.addLoginLog(new InternetProtocol(sd.getAddress()), login, 100, "login ok");

		logNotice("conexão da conta '%s' aceita.\n", sd.getUsername());

		sendCharServerList(sd);

		AuthNode node = new AuthNode();
		node.setAccountID(sd.getID());
		node.setSeed(sd.getSeed());
		node.getIP().set(sd.getAddress());
		node.setVersion(sd.getVersion());
		node.setClientType(sd.getClientType());

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer waitingDisconnect = timers.acquireTimer();
		waitingDisconnect.setTick(ts.getLastTick() + AUTH_TIMEOUT);
		waitingDisconnect.setListener(this.login.waitingDisconnectTimer);

		OnlineLogin online = new OnlineLogin();
		online.setAccountID(sd.getID());
		online.setWaitingDisconnect(waitingDisconnect);
		online.setCharServer(OnlineLogin.NONE);
		onlines.add(online);
	}

	/**
	 * Faz a autenticação para verificar se há algum servidor de personagens conectado.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @return true se houver ao menos um servidor ou false se não houver nenhum
	 */

	private boolean authServerConnected(LoginSessionData sd)
	{
		Account account = (Account) sd.getFileDescriptor().getCache();
		String username = account.getLogin().getUsername();

		int serverConnect = 0;
		LoginCharServers servers = getServer().getCharServers();

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
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
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
	 * Autentica uma conta em relação ao único grupo de contas habilitado a conectar.
	 * @param account referência da conta do cliente que deverá ser autenticada.
	 * @param required nível da conta necessária para se contar.
	 * @return true se estiver habilitado ou false caso contrário.
	 */

	private boolean authGroupToConnect(Account account, int required)
	{
		int group = account.getGroup().getCurrentGroup().getID();
		String username = account.getLogin().getUsername();

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
		String username = account.getLogin().getUsername();

		if (required == -1 && min < group)
			return true;

		logNotice("grupo '%d' é o mínimo aceito (usernme: %s, groupid: %d).\n", min, username, group);

		return false;
	}

	/**
	 * Autentica um cliente verificando se há um outro cliente usando a conta acessada.
	 * Caso haja um cliente usado a conta, deverá avisar quem está online e rejeitar o acesso.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 * @return true se não houver ninguém na conta online ou false caso contrário.
	 */

	private boolean authIsntOnline(LoginSessionData sd)
	{
		Account account = (Account) sd.getFileDescriptor().getCache();
		OnlineLogin online = onlines.get(account.getLogin().getID());

		if (online != null)
		{
			int id = online.getCharServerID();

			LoginCharServers servers = getServer().getCharServers();
			ClientCharServer server = servers.get(id);

			if (server != null)
			{
				authIsOnline(sd.getFileDescriptor(), account, online, server);
				return true;
			}

			auths.remove(sd.getID());
			onlines.remove(online);
		}

		return true;
	}

	/**
	 * A autenticação do cliente indicou que a conta já está sendo usada (online).
	 * Notificar ao cliente de que a conta que o servidor ainda o considera online.
	 * @param fd referência da conexão com o cliente para receber e enviar dados.
	 * @param account objeto contendo os detalhes da conta que está sendo acessada.
	 * @param online objeto que contém o gatilho para efetuar logout forçado.
	 * @param server servidor de personagens do qual a conta está online.
	 */

	private void authIsOnline(FileDescriptor fd, Account account, OnlineLogin online, ClientCharServer server)
	{
		logNotice("usuário '%s' já está online em '%s'.\n", account.getLogin().getUsername(), server.getName());

		AlreadyOnline packet = new AlreadyOnline();
		packet.setAccountID(account.getLogin().getID());

		sendAllWithoutOurSelf(packet);

		if (online.getWaitingDisconnect().getTick() == Timer.INVALID_TIMER)
		{
			TimerSystem ts = getTimerSystem();

			Timer timer = online.getWaitingDisconnect();
			timer.setListener(login.waitingDisconnectTimer);
			timer.setTick(ts.tick());
			ts.getTimers().add(timer);

			sendNotifyResult(fd, RECOGNIZES_LAST_LOGIN);

			return;
		}		
	}

	/**
	 * Chamado quando um servidor de personagens solicita a conexão com o servidor de acesso.
	 * @param fd referência da conexão com o cliente para enviar e receber dados.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 */

	private void requestCharConnect(FileDescriptor fd, LoginSessionData sd)
	{
		CharConnectReceive ccPacket = new CharConnectReceive();
		ccPacket.receive(fd);

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

		logInfo("conexão solicitada do servidor de personagens %s@%s (account: %s, pass: %s).\n", serverName, sd.getAddressString(), sd.getUsername(), sd.getPassword());

		String message = format("charserver - %s@%s:%d", serverName, SocketUtil.socketIP(serverIP), serverPort);
		log.addLoginLog(fd.getAddress(), sd, 100, message);

		AuthResult result = login.authLogin(sd, true);

		if (getServer().isState(ServerState.RUNNING) && result == AuthResult.OK && fd.isConnected())
		{
			log("conexão do servidor de personagens '%s' aceita.\n", serverName);

			ClientCharServer server = new ClientCharServer(fd);
			server.setFileDecriptor(fd);
			server.setName(serverName);
			server.setIP(new InternetProtocol(serverIP));
			server.setPort(serverPort);
			server.setUsers((short) 0);
			server.setType(CharServerType.parse(type));
			server.setNewDisplay(newDisplay);
			getServer().getCharServers().add(server);

			fd.setParseListener(character.parse);
			fd.getFlag().set(FileDescriptor.FLAG_SERVER);

			CharConnectResult packet = new CharConnectResult();
			packet.setResult(AuthResult.OK);
			packet.send(fd);
		}

		else
		{
			logNotice("Conexão com o servidor de personagens '%s' RECUSADA.\n", serverName);

			CharConnectResult packet = new CharConnectResult();
			packet.setResult(AuthResult.REJECTED_FROM_SERVER);
			packet.send(fd);
		}
	}
}

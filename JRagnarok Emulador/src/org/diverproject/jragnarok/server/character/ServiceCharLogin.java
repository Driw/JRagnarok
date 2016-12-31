package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.MIN_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.PACKETVER;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.hours;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MAINTANCE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MAX_USERS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_NEW_DISPLAY;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_OVERLOAD_BYPASS;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_SERVER_NAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_CHANGE_TIME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.PINCODE_FORCE;
import static org.diverproject.jragnarok.server.common.AuthResult.OK;
import static org.diverproject.jragnarok.server.common.DisconnectPlayer.KICK_ONLINE;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.lang.IntUtil.diff;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.character.toclient.HC_RefuseEnter;
import org.diverproject.jragnarok.packets.character.toclient.HC_SecondPasswordLogin.PincodeState;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AccountData;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AccountInfo;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AuthAccount;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_NotifyPinError;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_NotifyPinUpdate;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_SendAccount;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_SetAccountOffline;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_SetAccountOnline;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_SetAllAccountOffline;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_UpdateUserCount;
import org.diverproject.jragnarok.packets.inter.charmap.HZ_Ban;
import org.diverproject.jragnarok.packets.inter.charmap.HZ_ChangedSex;
import org.diverproject.jragnarok.packets.inter.charmap.HZ_SetUsersCount;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AccountData;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AccountInfo;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AckConnect;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AlreadyOnline;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AuthAccount;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_BanNotification;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_ChangeSex;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_KeepAlive;
import org.diverproject.jragnarok.packets.login.fromclient.CA_CharServerConnect;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorSystem;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.character.structures.ChangeSex;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.jragnarok.server.character.structures.ClientMapServer;
import org.diverproject.jragnarok.server.character.structures.OnlineCharData;
import org.diverproject.jragnarok.server.common.GlobalRegisterOperation;
import org.diverproject.jragnarok.server.common.NotifyAuthResult;
import org.diverproject.util.BitWise8;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.DynamicList;
import org.diverproject.util.lang.ByteUtil;

/**
 * <h1>Servi�o de Comunica��o do Servidor de Acesso</h1>
 *
 * <p>Todos os servidores de personagens devem passar por um servidor de acesso afim de autentic�-lo.
 * Ap�s ser autenticado por este, fica registrado no servidor de acesso com um servidor de personagem.
 * Este servi�o dever� garantir a total comunica��o do servidor de personagem com o de acesso.</p>
 *
 * <p>Aqui ser� estabelecido a conex�o com o servidor de acesso e um temporizador para garantir isto.
 * Caso o sistema desejar solicitar informa��es ao servidor de acesso para um cliente ou si mesmo,
 * essas solicita��es dever�o ser feitas atrav�s deste servi�o.</p>
 *
 * @see FileDescriptor
 * @see ServiceCharClient
 *
 * @author Andrew
 */

public class ServiceCharLogin extends AbstractCharService
{
	/**
	 * Tempo em milissegundos de espera m�xima para manter uma conex�o viva.
	 */
	public static final int STALL_TIME = seconds(60);


	/**
	 * Descritor de Arquivo para com o servidor de acesso.
	 */
	private CFileDescriptor fd;

	/**
	 * Servi�o para comunica��o inicial com o cliente.
	 */
	private ServiceCharClient client;

	/**
	 * Servi�o principal do servidor de personagem.
	 */
	private ServiceCharServer character;

	/**
	 * Servi�o para comunica��o com o servidor de mapa.
	 */
	private ServiceCharMap map;

	/**
	 * Servi�o para autentica��o dos jogadores no servidor.
	 */
	private ServiceCharServerAuth auth;

	/**
	 * Controle para autentica��o de jogadores online.
	 */
	private AuthMap auths;

	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineMap onlines;

	/**
	 * Controle para gerenciar dados dos personagens.
	 */
	private CharacterControl characters;

	/**
	 * Instancia um novo servi�o de comunica��o do servidor de personagem com o de acesso.
	 * Este servi�o possui depend�ncias portanto precisa ser iniciado e destru�do.
	 * @param server refer�ncia do servidor de personagem referente ao servi�o.
	 */

	public ServiceCharLogin(CharServer server)
	{
		super(server);
	}

	/**
	 * @return aquisi��o do descritor de arquivo para com o servidor de acesso.
	 */

	public CFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getCharClient();
		character = getServer().getFacade().getCharService();
		map = getServer().getFacade().getMapService();
		auth = getServer().getFacade().getAuthService();
		auths = getServer().getFacade().getAuthMap();
		onlines = getServer().getFacade().getOnlineMap();
		characters = getServer().getFacade().getCharacterControl();

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer connectionTimer = timers.acquireTimer();
		connectionTimer.setListener(CHECK_LOGIN_CONNECTION);
		connectionTimer.setTick(ts.getCurrentTime() + seconds(1));
		timers.addLoop(connectionTimer, seconds(10));

		Timer sendAccountsTimer = timers.acquireTimer();
		sendAccountsTimer.setListener(SEND_ACCOUNTS);
		sendAccountsTimer.setTick(ts.getCurrentTime() + seconds(1));
		timers.addLoop(sendAccountsTimer, hours(1));

		Timer timer = timers.acquireTimer();
		timer.setTick(ts.getCurrentTime() + seconds(1));
		timer.setListener(BROADCAST_USER_COUNT);
		timers.addLoop(timer, seconds(5));
	}

	/**
	 * Terminar esse servi�o inclui em fechar a conex�o com o servidor de acesso.
	 * Desta forma nenhum jogador poder� se comunicar/entrar nesse servidor.
	 * Uma vez que a conex�o seja fechada ela pode ser aberta novamente.
	 */

	public void destroy()
	{
		client = null;
		character = null;
		map = null;
		auth = null;
		auths = null;
		onlines = null;
		characters = null;

		if (fd != null)
		{
			fd.close();
			fd = null;
		}
	}

	/**
	 * Verifica se o servidor de personagens desse servi�o possui conex�o estabelecida.
	 * Essa conex�o � referente ao servidor de personagens, para ser listado no acesso.
	 * Caso esteja conectado um jogador ao conectar ter� esse servidor listado.
	 * @return true se estiver conectado ou false caso contr�rio.
	 */

	public boolean isConnected()
	{
		return fd != null && fd.isConnected();
	}

	/**
	 * Verifica sa h� conex�o com o servidor de acesso para enviar os dados do pacote.
	 * Se houver envia os dados de um pacote para um determinado descritor de arquivo.
	 * Caso contr�rio envia ao cliente um pacote mostrando que este foi rejeitado.
	 * @param fd c�digo de identifica��o da conex�o do cliente com o servidor.
	 * @param packet pacote contendo os dados do qual ser�o enviados.
	 * @return true se houver a conex�o com o servidor de acesso ou false caso contr�rio.
	 */

	public boolean sendPacket(CFileDescriptor fd, IResponsePacket packet)
	{
		if (!isConnected())
		{
			client.refuseEnter(fd, HC_RefuseEnter.REJECTED_FROM_SERVER);
			return false;
		}

		packet.send(getFileDescriptor());

		return true;
	}

	/**
	 * Listener usado atrav�s de um temporizador que tem como objeto garantir que o
	 * servidor de personagem esteja conectado com o servidor de acesso especificado.
	 * Caso j� exista uma conex�o, esse procedimento n�o ter� qualquer efeito.
	 */

	private final TimerListener CHECK_LOGIN_CONNECTION = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			if (isConnected())
				return;

			logInfo("tentando se conectar com o servidor de acesso...\n");

			String host = getConfigs().getString(LOGIN_IP);
			short port = s(getConfigs().getInt(LOGIN_PORT));

			try {

				Socket socket = new Socket(host, port);

				CFileDescriptor fd = new CFileDescriptor(socket);
				fd.getFlag().set(FileDescriptor.FLAG_SERVER);

				if (getFileDescriptorSystem().addFileDecriptor(fd))
				{
					String username = getConfigs().getString(CHAR_USERNAME);
					String password = getConfigs().getString(CHAR_PASSWORD);
					int serverIP = SocketUtil.socketIPInt(getConfigs().getString(CHAR_IP));
					short serverPort = s(getConfigs().getInt(CHAR_PORT));
					String serverName = getConfigs().getString(CHAR_SERVER_NAME);
					short type = s(getConfigs().getInt(CHAR_MAINTANCE));
					boolean newDisplay = getConfigs().getBool(CHAR_NEW_DISPLAY);

					CA_CharServerConnect packet = new CA_CharServerConnect();
					packet.setUsername(username);
					packet.setPassword(password);
					packet.setServerIP(serverIP);
					packet.setServerPort(serverPort);
					packet.setServerName(serverName);
					packet.setType(type);
					packet.setNewDisplay(newDisplay);
					packet.send(fd);
				}
				fd.setParseListener(getServer().getFacade().PARSE_LOGIN_SERVER);

			} catch (IOException e) {
				logInfo("falha ao conectar-se com %s:%d", host, port);
			}
		}
 		
		@Override
		public String getName()
		{
			return "checkLoginConnection";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	/**
	 * Listener usado pelo temporizador que comunica ao servidor de acesso os jogadores online.
	 * Ir� listar o c�digo de identifica��o de todas as contas que estiverem online.
	 */

	private final TimerListener SEND_ACCOUNTS = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			if (!isConnected())
				return;

			List<Integer> accounts = new DynamicList<>();

			for (OnlineCharData online : onlines)
				accounts.add(online.getAccountID());

			HA_SendAccount packet = new HA_SendAccount();
			packet.setAccounts(accounts);
			packet.send(getFileDescriptor());
		}
		
		@Override
		public String getName()
		{
			return "send_accounts_tologin";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	/**
	 * Listener usado para atualizar a quantidade de jogadores para todos os servidores.
	 * Envia ao servidor de acesso respectivo ao servidor de personagem em quest�o,
	 * como tamb�m para todos os servidores de mapa do mesmo (apenas se tiver alterado).
	 */

	private final TimerListener BROADCAST_USER_COUNT = new TimerListener()
	{
		private int previous;

		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			int users = character.getCountUsers();

			if (users == previous)
				return;

			previous = users;

			if (isConnected())
				updateUserCount(users);

			HZ_SetUsersCount packet = new HZ_SetUsersCount();
			packet.setUsers(users);

			map.sendAll(packet);
		}
		
		@Override
		public String getName()
		{
			return "broadcast_user_count";
		}
	};

	/**
	 * Verifica se a conex�o est� em estado para solicita��o de um ping.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void parsePing(CFileDescriptor fd)
	{
		if (fd.getFlag().is(FileDescriptor.FLAG_PING))
		{
			// Tempo de limite de espera alcan�ado - fechar conex�o
			if (diff(getTimerSystem().getCurrentTime(), fd.getTimeout()) > STALL_TIME * 2)
				fd.getFlag().set(FileDescriptor.FLAG_EOF);

			// Ping ainda n�o foi enviado - enviar
			else if (!fd.getFlag().is(FileDescriptor.FLAG_PING_SENT))
			{
				AH_KeepAlive packet = new AH_KeepAlive();
				packet.send(fd);

				fd.getFlag().set(FileDescriptor.FLAG_PING_SENT);
			}
		}		
	}

	/**
	 * Notifica uma determinada conex�o de um cliente que sua conta acaba de ser banida no sistema.
	 * Ser� identificado o c�digo da conta do jogador, o tipo de altera��o e at� quando ser� banido.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void banNofitication(CFileDescriptor fd)
	{
		AH_BanNotification packet = new AH_BanNotification();
		packet.receive(fd);

		HZ_Ban result = new HZ_Ban();
		result.setAccountID(packet.getAccountID());
		result.setType(packet.getType());
		result.setUnbanTime(packet.getUnbanTime());

		map.sendAll(result);
		character.disconnectPlayer(packet.getAccountID());
	}

	/**
	 * Notifica um jogador em um servidor de mapas que sua conta est� sendo usada por outro jogador.
	 * Esse chamado ocorre quando um jogador tenta entrar em uma conta que j� se encontra online.
	 * @param fd conex�o do descritor de arquivo da conex�o com o servidor de mapas.
	 */

	public void alreadyOnline(CFileDescriptor sfd)
	{
		AH_AlreadyOnline packet = new AH_AlreadyOnline();
		packet.receive(sfd);

		OnlineCharData online = onlines.get(packet.getAccountID());

		if (online != null)
		{
			// Conta j� marcada como online
			if (online.getServer() > OnlineCharData.NO_SERVER)
			{
				ClientMapServer server = getServer().getMapServers().get(online.getServer());
				map.disconnectPlayer(server.getFileDecriptor(), online.getCharID(), KICK_ONLINE);

				TimerSystem ts = getTimerSystem();
				TimerMap timers = ts.getTimers();
				timers.delete(online.getWaitingDisconnect());

				Timer waitingDisconnect = timers.acquireTimer();
				waitingDisconnect.setTick(ts.getCurrentTime());
				waitingDisconnect.setObjectID(online.getAccountID());
				waitingDisconnect.setListener(character.WAITING_DISCONNECT);
				timers.addInterval(waitingDisconnect, ServiceCharServer.AUTH_TIMEOUT);
			}

			else
			{
				CFileDescriptor fd = null;

				for (FileDescriptor ifd : getFileDescriptorSystem())
					if (fd instanceof CFileDescriptor)
					{
						CFileDescriptor cfd = (CFileDescriptor) ifd;

						if (cfd.getSessionData().getID() == packet.getAccountID())
						{
							fd = cfd;
							break;
						}
					}

				if (fd != null)
					character.setCharOffline(-1, packet.getAccountID());
				else
				{
					client.sendNotifyResult(fd, NotifyAuthResult.ALREADY_ONLINE);
					FileDescriptorSystem.setEndOfFile(fd);
				}
			}
		}

		auths.remove(packet.getAccountID());
	}

	/**
	 * Analisa a resposta de uma solicita��o para se conectar com o servidor de acesso.
	 * @param fd conex�o do descritor de arquivo da conex�o com o servidor de acesso.
	 * @return true para manter a conex�o aberta ou false para fechar.
	 */

	public boolean parseLoginResult(CFileDescriptor fd)
	{
		AH_AckConnect packet = new AH_AckConnect();
		packet.receive(fd, false);

		if (packet.getResult() == OK)
			this.fd = fd;

		return packet.getResult() == OK;
	}

	/**
	 * Solicita ao servidor de acesso a autentica��o de uma determinada conta acessada.
	 * Essa autentica��o fica no servidor de acesso at� que o de personagem solicite.
	 * Em �ltimo caso pode ser removido tamb�m devido a inatividade do jogador.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conex�o aberta ou false para fechar.
	 */

	public boolean reqAuthAccount(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("solicitando autentica��o de conta (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		HA_AuthAccount packet = new HA_AuthAccount();
		packet.setFileDescriptorID(fd.getID());
		packet.setAccountID(sd.getID());
		packet.setFirstSeed(sd.getSeed().getFirst());
		packet.setSecondSeed(sd.getSeed().getSecond());
		packet.setIP(fd.getAddress());

		return sendPacket(fd, packet);
	}

	/**
	 * Recebe o resultado obtido do servidor de acesso da autentica��o de uma conta.
	 * @param lfd conex�o do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true para manter a conex�o aberta ou false para fechar.
	 */

	public boolean parseAuthAccount(CFileDescriptor lfd)
	{
		AH_AuthAccount packet = new AH_AuthAccount();
		packet.receive(lfd);

		logDebug("recebendo resultado da autentica��o de conta (aid: %d).\n", packet.getAccountID());

		int fdID = packet.getFileDescriptorID();

		if (getFileDescriptorSystem().isAlive(fdID))
		{
			CFileDescriptor fd = (CFileDescriptor) getFileDescriptorSystem().get(fdID);
			CharSessionData sd = fd.getSessionData();

			if (!sd.isAuth() && sd.getID() == packet.getAccountID() &&
				sd.getSeed().equals(packet.getFirstSeed(), packet.getSecondSeed()))
			{
				sd.setVersion(packet.getVersion());
				sd.setClientType(packet.getClientType());

				int serverVersion = dateToVersion(PACKETVER);

				if (sd.getVersion() != serverVersion)
					logWarning("account#%d com vers�o %d e o servidor foi compilado para %d.\n", sd.getID(), sd.getVersion(), serverVersion);

				if (packet.isResult())
					return auth.authOk(fd);

				client.refuseEnter(fd, HC_RefuseEnter.REJECTED_FROM_SERVER);
			}
		}

		return true;
	}

	/**
	 * Faz uma solicita��o ao servidor de acesso sobre os dados de uma conta.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conex�o aberta ou false para fechar.
	 */

	public boolean reqAccountData(CFileDescriptor fd)
	{
		logDebug("solicitando dados de conta (fd: %d).\n", fd.getID());

		CharSessionData sd = fd.getSessionData();
		HA_AccountData packet = new HA_AccountData();
		packet.setFdID(fd.getID());
		packet.setAccountID(sd.getID());

		return sendPacket(fd, packet);
	}

	/**
	 * Recebe os dados de uma conta do servidor de acesso que foi solicitada.
	 * @param lfd conex�o do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true para manter a conex�o aberta ou false para fechar.
	 */

	public boolean parseAccountData(CFileDescriptor lfd)
	{
		logDebug("recebendo resultado dos dados de conta.\n");

		AH_AccountData packet = new AH_AccountData();
		packet.receive(lfd);

		CFileDescriptor fd = (CFileDescriptor) getFileDescriptorSystem().get(packet.getFdID());
		CharSessionData sd = fd.getSessionData();

		if (!sd.isAuth() || sd.getID() != packet.getAccountID())
			return false;

		sd.setEmail(packet.getEmail());
		sd.getExpiration().set(packet.getExpirationTime());

		if (MAX_CHARS != 0 && packet.getCharSlots() > MAX_CHARS)
			logWarning("limite de personagens permitidos por conta (aid: %d)", sd.getID());

		sd.setCharSlots(ByteUtil.limit(packet.getCharSlots(), b(MIN_CHARS), b(MAX_CHARS)));
		sd.setBirthdate(packet.getBirthdate());

		// TODO sd.setGroup(group);
		// TODO sd.setPincode(pincode);
		// TODO sd.setVip(vip);

		OnlineCharData online = onlines.get(sd.getID());
		boolean enabled = false;

		if (online != null)
		{
			int maxUsers = getConfigs().getInt(CHAR_MAX_USERS);
			int overloadBypass = getConfigs().getInt(CHAR_OVERLOAD_BYPASS);

			ClientMapServer server = getServer().getMapServers().get(online.getServer());

			if (server != null && server.getFileDecriptor().isConnected())
			{
				if (maxUsers == 0 && sd.getGroup().getAccessLevel() >= overloadBypass)
					enabled = true;

				else if (maxUsers > 0 && character.getCountUsers() >= maxUsers && sd.getGroup().getAccessLevel() >= overloadBypass)
					enabled = true;
			}
		}

		enabled = true; // Remover depois de identificar o servidor de mapa.

		if (enabled)
		{
			client.sendCharList(fd);

			if (sd.getVersion() >= dateToVersion(20110309))
				pincodeStart(fd);
		}

		else
			client.refuseEnter(fd, HC_RefuseEnter.REJECTED_FROM_SERVER);

		return enabled;
	}

	/**
	 * Solicita ao servidor de acesso informa��es b�sicas de uma conta especifica.
	 * A conta em quest�o ser� aquela utilizada pelo jogador da conex�o abaixo:
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se conseguir enviar a solicita��o ou false caso contr�rio.
	 */

	public boolean reqAccountInfo(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("solicitando informa��es de conta (fd: %d, aid: %d).\n", sd.getID());

		HA_AccountInfo packet = new HA_AccountInfo();
		packet.setServerFD(getFileDescriptor().getID());
		packet.setUserFD(sd.getID());
		packet.setAccountID(sd.getID());

		return sendPacket(fd, packet);
	}

	/**
	 * Recebe do servidor de acesso as informa��es b�sicas da conta de um jogador conectado.
	 * @param lfd conex�o do descritor de arquivo do servidor de acesso com o servidor.
	 */

	public void parseAccountInfo(CFileDescriptor lfd)
	{
		AH_AccountInfo packet = new AH_AccountInfo();
		packet.receive(lfd);

		logDebug("recebendo informa��es da conta do servidor de acesso (aid: %d).\n", packet.getAccountID());

		map.receiveAccountInfo(packet);
	}

	/**
	 * Mant�m a conex�o de um descritor de arquivo vida dentro do sistema com um "ping".
	 * @param lfd conex�o do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se ainda estiver conectado ou false caso contr�rio.
	 */

	public boolean keepAlive(CFileDescriptor lfd)
	{
		logDebug("recebendo ping do servidor de acesso (ip: %s).\n", getFileDescriptor().getAddressString());

		lfd.getFlag().unset(FileDescriptor.FLAG_PING);

		return lfd.isConnected();
	}

	/**
	 * Recebe a solicita��o para altera��o do sexo dos personagens de uma determinada conta.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se conseguir alterar ou false caso a conta n�o tenha sido definida.
	 */

	public boolean reqChangeSex(CFileDescriptor fd)
	{
		AH_ChangeSex packet = new AH_ChangeSex();
		packet.receive(fd);

		logDebug("solicitado alterar sexo dos personagens (fd: %d, aid: %d).\n", fd.getID(), packet.getAccountID());

		if (packet.getAccountID() > 0)
		{
			try {

				List<ChangeSex> changes = characters.listChangeSex(packet.getAccountID());

				for (ChangeSex change : changes)
					setCharSex(change, packet.getSex());

				return true;

			} catch (RagnarokException e) {

				logError("falha ao tentar alterar o sexo dos personagens (fd: %d, aid: %d):.\n", fd.getID(), packet.getAccountID());
				logExeception(e);
			}
		}

		return false;
	}

	/**
	 * Faz a altera��o do sexo de um determinado personagem e notifica aos servidores.
	 * @param charID c�digo de identifica��o do personagem que ter� o sexo alterado.
	 * @param sex caracter que representa o seu novo sexo (M: masculino, F: feminino).
	 * @return true se for alterado com sucesso ou false se n�o encontrar o personagem.
	 */

	public boolean parseChangeCharSex(int charID, char sex)
	{
		try {

			if (!characters.setSex(charID, sex))
				return false;

			characters.setSex(charID, sex);
			ChangeSex change = characters.getChangeSex(charID);

			setCharSex(change, sex);
			character.disconnectPlayer(change.getAccountID());

			HZ_ChangedSex packet = new HZ_ChangedSex();
			packet.setAccountID(change.getAccountID());
			packet.setSex(sex);

			map.sendAll(packet);

			return true;

		} catch (RagnarokException e) {

			logError("falha ao tentar alterar o sexo do personagem (charid: %d);\n", charID);
			logExeception(e);

			return false;
		}
	}

	/**
	 * TODO
	 * @param change
	 * @param sex
	 */

	private void setCharSex(ChangeSex change, char sex)
	{
		// TODO chlogif_parse_change_sex_sub
		
	}

	/**
	 * TODO
	 * @param fd
	 */

	public void reqGlobalAccountReg(CFileDescriptor fd)
	{
		
	}

	/**
	 * TODO
	 * @param register
	 */

	public void sendGlobalAccountReg(GlobalRegisterOperation<?> register)
	{
		// TODO chlogif_send_global_accreg
		
	}

	/**
	 * TODO
	 * @param aid
	 * @param flag
	 * @param timeDiff
	 * @param mapFD
	 * @return
	 */

	public boolean reqVipData(int aid, BitWise8 flag, int timeDiff, int mapFD)
	{
		// TODO chlogif_reqvipdata

		return false;
	}

	/**
	 * TODO
	 * @param fd
	 * @return
	 */

	public boolean parseVipData(CFileDescriptor fd)
	{
		// TODO chlogif_parse_vipack

		return false;
	}

	/**
	 * Analise a situa��o do sistema de c�digo PIN conforme configura��es e estado do jogador.
	 * Se estiver habilitado antes de tudo verifica se a conta possuir um c�digo PIn definido
	 * ou ainda ent�o se a conta deseja utilizar o sistema de c�digo PIN como seguran�a extra.
	 * Se n�o houver solicita a cria��o de um c�digo PIN ao mesmo, caso j� esteja definido
	 * verifica se o c�digo PIN j� n�o expirou (se habilitado a expira��o), se estiver ir�
	 * solicitar a altera��o do c�digo PIN, caso passe por tudo isso solicita o c�digo PIN.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void pincodeStart(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		if (!getConfigs().getBool(PINCODE_ENABLED))
		{
			logDebug("sistema de c�digo pin iniciado (aid: %d).\n", sd.getID());

			// N�o h� c�digo PIN definido
			if (sd.getPincode().getCode() == null)
			{
				if (getConfigs().getBool(PINCODE_FORCE))
					client.pincodeSendState(fd, PincodeState.NEW);
				else
					client.pincodeSendState(fd, PincodeState.SKIP);
			}

			// C�digo PIN definido mas n�o habilitado
			else if (!sd.getPincode().isEnabled())
				client.pincodeSendState(fd, PincodeState.OK);

			// C�digo PIN habilitado e definido
			else
			{
				int changeTime = getConfigs().getInt(PINCODE_CHANGE_TIME);

				if (changeTime > 0 && sd.getPincode().getChanged().pass(changeTime))
					client.pincodeSendState(fd, PincodeState.EXPIRED);

				else
				{
					OnlineCharData online = onlines.get(sd.getID());

					if (online != null && online.isPincodeSuccess())
						client.pincodeSendState(fd, PincodeState.SKIP);
					else
						client.pincodeSendState(fd, PincodeState.ASK);
				}
			}
		}

		else
			client.pincodeSendState(fd, PincodeState.OK);
	}

	/**
	 * Notifica ao servidor de acesso que o c�digo pin inserido estava incorreto.
	 * Essa a��o dever� resultar no fechamento da conex�o com o servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @return true se houver conex�o com o servidor de acesso ou false caso contr�rio.
	 */

	public boolean notifyLoginPinError(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("notificar c�digo pin incorreto (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		HA_NotifyPinError packet = new HA_NotifyPinError();
		packet.setAccountID(sd.getID());
		packet.send(getFileDescriptor());

		return sendPacket(fd, packet);
	}

	/**
	 * Solicitar ao servidor de acesso que atualize do c�digo PIN de uma conta.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param pincode novo c�digo pin do qual a conta dever� assumir.
	 * @return true se houver conex�o com o servidor de acesso ou false caso contr�rio.
	 */

	public boolean notifyLoginPinUpdate(CFileDescriptor fd, String pincode)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("notificar atualiza��o de c�digo pin (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		HA_NotifyPinUpdate packet = new HA_NotifyPinUpdate();
		packet.setAccountID(sd.getID());
		packet.setPincode(pincode);

		return sendPacket(fd, packet);
	}

	/**
	 * Envia ao servidor de acesso a quantidade de jogadores online no servidor.
	 * @param users quantidade atual de jogadores online no servidor.
	 * @return true para manter a conex�o aberta ou false para fechar.
	 */

	public boolean updateUserCount(int users)
	{
		logDebug("atualizar contagem de jogadores online.\n", fd.getID());

		HA_UpdateUserCount packet = new HA_UpdateUserCount();
		packet.setCount(users);

		return sendPacket(fd, packet);
	}

	/**
	 * Envia ao servidor de acesso em que se est� conectado para definir todas as contas como offline.
	 */

	public void setAllAccountOffline()
	{
		if (isConnected())
		{
			logDebug("solicitado ao servidor de acesso para todas as contas ficarem offline.\n");

			HA_SetAllAccountOffline packet = new HA_SetAllAccountOffline();
			packet.send(getFileDescriptor());
		}
	}

	/**
	 * Envia ao servidor de acesso em que se est� conectado para definir uma conta como offline.
	 * @param accountID c�digo de identifica��o da conta que ficar� offline no servidor.
	 */

	public void setAccountOffline(int accountID)
	{
		if (isConnected())
		{
			logDebug("solicitado ao servidor de acesso para account#%d ficar offline.\n", accountID);

			HA_SetAccountOffline packet = new HA_SetAccountOffline();
			packet.setAccountID(accountID);
			packet.send(getFileDescriptor());
		}
	}

	/**
	 * Envia ao servidor de acesso em que se est� conectado para definir uma conta como online.
	 * @param accountID c�digo de identifica��o da conta que ficar� online no servidor.
	 */

	public void setAccountOnline(int accountID)
	{
		if (isConnected())
		{
			logDebug("solicitado ao servidor de acesso para account#%d ficar online.\n", accountID);

			HA_SetAccountOnline packet = new HA_SetAccountOnline();
			packet.setAccountID(accountID);
			packet.send(getFileDescriptor());
		}
	}
}

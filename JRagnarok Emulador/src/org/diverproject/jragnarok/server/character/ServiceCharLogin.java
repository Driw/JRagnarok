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
 * <h1>Serviço de Comunicação do Servidor de Acesso</h1>
 *
 * <p>Todos os servidores de personagens devem passar por um servidor de acesso afim de autenticá-lo.
 * Após ser autenticado por este, fica registrado no servidor de acesso com um servidor de personagem.
 * Este serviço deverá garantir a total comunicação do servidor de personagem com o de acesso.</p>
 *
 * <p>Aqui será estabelecido a conexão com o servidor de acesso e um temporizador para garantir isto.
 * Caso o sistema desejar solicitar informações ao servidor de acesso para um cliente ou si mesmo,
 * essas solicitações deverão ser feitas através deste serviço.</p>
 *
 * @see FileDescriptor
 * @see ServiceCharClient
 *
 * @author Andrew
 */

public class ServiceCharLogin extends AbstractCharService
{
	/**
	 * Tempo em milissegundos de espera máxima para manter uma conexão viva.
	 */
	public static final int STALL_TIME = seconds(60);


	/**
	 * Descritor de Arquivo para com o servidor de acesso.
	 */
	private CFileDescriptor fd;

	/**
	 * Serviço para comunicação inicial com o cliente.
	 */
	private ServiceCharClient client;

	/**
	 * Serviço principal do servidor de personagem.
	 */
	private ServiceCharServer character;

	/**
	 * Serviço para comunicação com o servidor de mapa.
	 */
	private ServiceCharMap map;

	/**
	 * Serviço para autenticação dos jogadores no servidor.
	 */
	private ServiceCharServerAuth auth;

	/**
	 * Controle para autenticação de jogadores online.
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
	 * Instancia um novo serviço de comunicação do servidor de personagem com o de acesso.
	 * Este serviço possui dependências portanto precisa ser iniciado e destruído.
	 * @param server referência do servidor de personagem referente ao serviço.
	 */

	public ServiceCharLogin(CharServer server)
	{
		super(server);
	}

	/**
	 * @return aquisição do descritor de arquivo para com o servidor de acesso.
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
	 * Terminar esse serviço inclui em fechar a conexão com o servidor de acesso.
	 * Desta forma nenhum jogador poderá se comunicar/entrar nesse servidor.
	 * Uma vez que a conexão seja fechada ela pode ser aberta novamente.
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
	 * Verifica se o servidor de personagens desse serviço possui conexão estabelecida.
	 * Essa conexão é referente ao servidor de personagens, para ser listado no acesso.
	 * Caso esteja conectado um jogador ao conectar terá esse servidor listado.
	 * @return true se estiver conectado ou false caso contrário.
	 */

	public boolean isConnected()
	{
		return fd != null && fd.isConnected();
	}

	/**
	 * Verifica sa há conexão com o servidor de acesso para enviar os dados do pacote.
	 * Se houver envia os dados de um pacote para um determinado descritor de arquivo.
	 * Caso contrário envia ao cliente um pacote mostrando que este foi rejeitado.
	 * @param fd código de identificação da conexão do cliente com o servidor.
	 * @param packet pacote contendo os dados do qual serão enviados.
	 * @return true se houver a conexão com o servidor de acesso ou false caso contrário.
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
	 * Listener usado através de um temporizador que tem como objeto garantir que o
	 * servidor de personagem esteja conectado com o servidor de acesso especificado.
	 * Caso já exista uma conexão, esse procedimento não terá qualquer efeito.
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
	 * Irá listar o código de identificação de todas as contas que estiverem online.
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
	 * Envia ao servidor de acesso respectivo ao servidor de personagem em questão,
	 * como também para todos os servidores de mapa do mesmo (apenas se tiver alterado).
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
	 * Verifica se a conexão está em estado para solicitação de um ping.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void parsePing(CFileDescriptor fd)
	{
		if (fd.getFlag().is(FileDescriptor.FLAG_PING))
		{
			// Tempo de limite de espera alcançado - fechar conexão
			if (diff(getTimerSystem().getCurrentTime(), fd.getTimeout()) > STALL_TIME * 2)
				fd.getFlag().set(FileDescriptor.FLAG_EOF);

			// Ping ainda não foi enviado - enviar
			else if (!fd.getFlag().is(FileDescriptor.FLAG_PING_SENT))
			{
				AH_KeepAlive packet = new AH_KeepAlive();
				packet.send(fd);

				fd.getFlag().set(FileDescriptor.FLAG_PING_SENT);
			}
		}		
	}

	/**
	 * Notifica uma determinada conexão de um cliente que sua conta acaba de ser banida no sistema.
	 * Será identificado o código da conta do jogador, o tipo de alteração e até quando será banido.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * Notifica um jogador em um servidor de mapas que sua conta está sendo usada por outro jogador.
	 * Esse chamado ocorre quando um jogador tenta entrar em uma conta que já se encontra online.
	 * @param fd conexão do descritor de arquivo da conexão com o servidor de mapas.
	 */

	public void alreadyOnline(CFileDescriptor sfd)
	{
		AH_AlreadyOnline packet = new AH_AlreadyOnline();
		packet.receive(sfd);

		OnlineCharData online = onlines.get(packet.getAccountID());

		if (online != null)
		{
			// Conta já marcada como online
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
	 * Analisa a resposta de uma solicitação para se conectar com o servidor de acesso.
	 * @param fd conexão do descritor de arquivo da conexão com o servidor de acesso.
	 * @return true para manter a conexão aberta ou false para fechar.
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
	 * Solicita ao servidor de acesso a autenticação de uma determinada conta acessada.
	 * Essa autenticação fica no servidor de acesso até que o de personagem solicite.
	 * Em último caso pode ser removido também devido a inatividade do jogador.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conexão aberta ou false para fechar.
	 */

	public boolean reqAuthAccount(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("solicitando autenticação de conta (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		HA_AuthAccount packet = new HA_AuthAccount();
		packet.setFileDescriptorID(fd.getID());
		packet.setAccountID(sd.getID());
		packet.setFirstSeed(sd.getSeed().getFirst());
		packet.setSecondSeed(sd.getSeed().getSecond());
		packet.setIP(fd.getAddress());

		return sendPacket(fd, packet);
	}

	/**
	 * Recebe o resultado obtido do servidor de acesso da autenticação de uma conta.
	 * @param lfd conexão do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true para manter a conexão aberta ou false para fechar.
	 */

	public boolean parseAuthAccount(CFileDescriptor lfd)
	{
		AH_AuthAccount packet = new AH_AuthAccount();
		packet.receive(lfd);

		logDebug("recebendo resultado da autenticação de conta (aid: %d).\n", packet.getAccountID());

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
					logWarning("account#%d com versão %d e o servidor foi compilado para %d.\n", sd.getID(), sd.getVersion(), serverVersion);

				if (packet.isResult())
					return auth.authOk(fd);

				client.refuseEnter(fd, HC_RefuseEnter.REJECTED_FROM_SERVER);
			}
		}

		return true;
	}

	/**
	 * Faz uma solicitação ao servidor de acesso sobre os dados de uma conta.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conexão aberta ou false para fechar.
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
	 * @param lfd conexão do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true para manter a conexão aberta ou false para fechar.
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
	 * Solicita ao servidor de acesso informações básicas de uma conta especifica.
	 * A conta em questão será aquela utilizada pelo jogador da conexão abaixo:
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se conseguir enviar a solicitação ou false caso contrário.
	 */

	public boolean reqAccountInfo(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("solicitando informações de conta (fd: %d, aid: %d).\n", sd.getID());

		HA_AccountInfo packet = new HA_AccountInfo();
		packet.setServerFD(getFileDescriptor().getID());
		packet.setUserFD(sd.getID());
		packet.setAccountID(sd.getID());

		return sendPacket(fd, packet);
	}

	/**
	 * Recebe do servidor de acesso as informações básicas da conta de um jogador conectado.
	 * @param lfd conexão do descritor de arquivo do servidor de acesso com o servidor.
	 */

	public void parseAccountInfo(CFileDescriptor lfd)
	{
		AH_AccountInfo packet = new AH_AccountInfo();
		packet.receive(lfd);

		logDebug("recebendo informações da conta do servidor de acesso (aid: %d).\n", packet.getAccountID());

		map.receiveAccountInfo(packet);
	}

	/**
	 * Mantém a conexão de um descritor de arquivo vida dentro do sistema com um "ping".
	 * @param lfd conexão do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se ainda estiver conectado ou false caso contrário.
	 */

	public boolean keepAlive(CFileDescriptor lfd)
	{
		logDebug("recebendo ping do servidor de acesso (ip: %s).\n", getFileDescriptor().getAddressString());

		lfd.getFlag().unset(FileDescriptor.FLAG_PING);

		return lfd.isConnected();
	}

	/**
	 * Recebe a solicitação para alteração do sexo dos personagens de uma determinada conta.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se conseguir alterar ou false caso a conta não tenha sido definida.
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
	 * Faz a alteração do sexo de um determinado personagem e notifica aos servidores.
	 * @param charID código de identificação do personagem que terá o sexo alterado.
	 * @param sex caracter que representa o seu novo sexo (M: masculino, F: feminino).
	 * @return true se for alterado com sucesso ou false se não encontrar o personagem.
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
	 * Analise a situação do sistema de código PIN conforme configurações e estado do jogador.
	 * Se estiver habilitado antes de tudo verifica se a conta possuir um código PIn definido
	 * ou ainda então se a conta deseja utilizar o sistema de código PIN como segurança extra.
	 * Se não houver solicita a criação de um código PIN ao mesmo, caso já esteja definido
	 * verifica se o código PIN já não expirou (se habilitado a expiração), se estiver irá
	 * solicitar a alteração do código PIN, caso passe por tudo isso solicita o código PIN.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void pincodeStart(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		if (!getConfigs().getBool(PINCODE_ENABLED))
		{
			logDebug("sistema de código pin iniciado (aid: %d).\n", sd.getID());

			// Não há código PIN definido
			if (sd.getPincode().getCode() == null)
			{
				if (getConfigs().getBool(PINCODE_FORCE))
					client.pincodeSendState(fd, PincodeState.NEW);
				else
					client.pincodeSendState(fd, PincodeState.SKIP);
			}

			// Código PIN definido mas não habilitado
			else if (!sd.getPincode().isEnabled())
				client.pincodeSendState(fd, PincodeState.OK);

			// Código PIN habilitado e definido
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
	 * Notifica ao servidor de acesso que o código pin inserido estava incorreto.
	 * Essa ação deverá resultar no fechamento da conexão com o servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se houver conexão com o servidor de acesso ou false caso contrário.
	 */

	public boolean notifyLoginPinError(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("notificar código pin incorreto (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		HA_NotifyPinError packet = new HA_NotifyPinError();
		packet.setAccountID(sd.getID());
		packet.send(getFileDescriptor());

		return sendPacket(fd, packet);
	}

	/**
	 * Solicitar ao servidor de acesso que atualize do código PIN de uma conta.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param pincode novo código pin do qual a conta deverá assumir.
	 * @return true se houver conexão com o servidor de acesso ou false caso contrário.
	 */

	public boolean notifyLoginPinUpdate(CFileDescriptor fd, String pincode)
	{
		CharSessionData sd = fd.getSessionData();

		logDebug("notificar atualização de código pin (fd: %d, aid: %d).\n", fd.getID(), sd.getID());

		HA_NotifyPinUpdate packet = new HA_NotifyPinUpdate();
		packet.setAccountID(sd.getID());
		packet.setPincode(pincode);

		return sendPacket(fd, packet);
	}

	/**
	 * Envia ao servidor de acesso a quantidade de jogadores online no servidor.
	 * @param users quantidade atual de jogadores online no servidor.
	 * @return true para manter a conexão aberta ou false para fechar.
	 */

	public boolean updateUserCount(int users)
	{
		logDebug("atualizar contagem de jogadores online.\n", fd.getID());

		HA_UpdateUserCount packet = new HA_UpdateUserCount();
		packet.setCount(users);

		return sendPacket(fd, packet);
	}

	/**
	 * Envia ao servidor de acesso em que se está conectado para definir todas as contas como offline.
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
	 * Envia ao servidor de acesso em que se está conectado para definir uma conta como offline.
	 * @param accountID código de identificação da conta que ficará offline no servidor.
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
	 * Envia ao servidor de acesso em que se está conectado para definir uma conta como online.
	 * @param accountID código de identificação da conta que ficará online no servidor.
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

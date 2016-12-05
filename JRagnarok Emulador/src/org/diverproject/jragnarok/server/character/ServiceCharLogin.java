package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.PACKETVER;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.hours;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_MAINTANCE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_NEW_DISPLAY;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_SERVER_NAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PORT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_BAN_NOTIFICATION;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ALREADY_ONLINE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_CHANGE_SEX;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_GLOBAL_ACCREG;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_KEEP_ALIVE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_VIP;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_ACCOUNT_DATA;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_ACCOUNT_INFO;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_AUTH_ACCOUNT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_CHAR_SERVER_CONNECT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SYNCRONIZE_IPADDRESS;
import static org.diverproject.jragnarok.server.common.AuthResult.OK;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.lang.IntUtil.diff;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.receive.AcknowledgePacket;
import org.diverproject.jragnarok.packets.request.AccountDataRequest;
import org.diverproject.jragnarok.packets.request.AuthAccountRequest;
import org.diverproject.jragnarok.packets.request.AuthAccountResult;
import org.diverproject.jragnarok.packets.request.CharMapUserCountRequest;
import org.diverproject.jragnarok.packets.request.CharServerConnectRequest;
import org.diverproject.jragnarok.packets.request.CharServerConnectResult;
import org.diverproject.jragnarok.packets.request.SendAccountRequest;
import org.diverproject.jragnarok.packets.request.UpdateUserCount;
import org.diverproject.jragnarok.packets.response.KeepAliveRequest;
import org.diverproject.jragnarok.packets.response.RefuseEnter;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.jragnarok.server.character.structures.OnlineCharData;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.DynamicList;

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
	 * TODO
	 */
	protected static final int STALL_TIME = 60;

	/**
	 * Descritor de Arquivo para com o servidor de acesso.
	 */
	private CFileDescriptor fd;

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
		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer connectionTimer = timers.acquireTimer();
		connectionTimer.setListener(checkLoginConnection);
		connectionTimer.setTick(ts.getCurrentTime() + seconds(1));
		timers.addLoop(connectionTimer, seconds(10));

		Timer sendAccountsTimer = timers.acquireTimer();
		sendAccountsTimer.setListener(sendAccounts);
		sendAccountsTimer.setTick(ts.getCurrentTime() + seconds(1));
		timers.addLoop(sendAccountsTimer, hours(1));

		Timer timer = timers.acquireTimer();
		timer.setTick(ts.getCurrentTime() + seconds(1));
		timer.setListener(broadcastUserCount);
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

	// TODO public void onDisconnect;

	/**
	 * Listener usado através de um temporizador que tem como objeto garantir que o
	 * servidor de personagem esteja conectado com o servidor de acesso especificado.
	 * Caso já exista uma conexão, esse procedimento não terá qualquer efeito.
	 */

	private final TimerListener checkLoginConnection = new TimerListener()
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
					short newDisplay = s(getConfigs().getInt(CHAR_NEW_DISPLAY));

					CharServerConnectRequest packet = new CharServerConnectRequest();
					packet.setUsername(username);
					packet.setPassword(password);
					packet.setServerIP(serverIP);
					packet.setServerPort(serverPort);
					packet.setServerName(serverName);
					packet.setType(type);
					packet.setNewDisplay(newDisplay);
					packet.send(fd);
				}
				fd.setParseListener(parse);

			} catch (IOException e) {
				logInfo("falha ao conectar-se com %s:%d", host, port);
			}
		}
 		
		@Override
		public String getName()
		{
			return "check_connect_login_server";
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

	private final TimerListener sendAccounts = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			List<Integer> accounts = new DynamicList<>();

			for (OnlineCharData online : onlines)
				accounts.add(online.getAccountID());

			SendAccountRequest packet = new SendAccountRequest();
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

	private final TimerListener broadcastUserCount = new TimerListener()
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

			CharMapUserCountRequest packet = new CharMapUserCountRequest();
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
	 * Listener que será definido para a conexão estabelecida com o servidor de acesso.
	 * Ele deverá receber todos os comandos ou resultados de solicitações do mesmo.
	 * Após receber o comando deverá despachar para o procedimento correto.
	 */

	private final FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logWarning("conexão com o servidor de personagens perdida.\n");
				return false;
			}

			CFileDescriptor cfd = (CFileDescriptor) fd;
			parsePing(cfd);

			return parseCommand(cfd);
		}
	};

	/**
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	private void parsePing(CFileDescriptor fd)
	{
		if (fd.getFlag().is(FileDescriptor.FLAG_PING))
		{
			if (diff(getTimerSystem().getCurrentTime(), fd.getTimeout()) > STALL_TIME * 2)
				fd.getFlag().set(FileDescriptor.FLAG_EOF);

			else if (!fd.getFlag().is(FileDescriptor.FLAG_PING_SENT))
			{
				KeepAliveRequest packet = new KeepAliveRequest();
				packet.send(fd);

				fd.getFlag().set(FileDescriptor.FLAG_PING_SENT);
			}
		}		
	}

	/**
	 * Procedimento que deverá verificar qual o código do comando passado pelo servidor de acesso.
	 * A partir deste comando deverá repassar a conexão para o procedimento correto.
	 * @param fd conexão do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se deve manter a conexão ou false para encerrar.
	 */

	protected boolean parseCommand(CFileDescriptor fd)
	{
		AcknowledgePacket ack = new AcknowledgePacket();
		ack.receive(fd, false);

		short command = ack.getPacketID();

		switch (command)
		{
			case PACKET_RES_CHAR_SERVER_CONNECT: return parseLoginResult(fd);
			case PACKET_RES_AUTH_ACCOUNT: return parseAuthAccount(fd);
			case PACKET_RES_ACCOUNT_DATA: return parseAccountData(fd);
			case PACKET_REQ_KEEP_ALIVE: return keepAlive(fd);
			case PACKET_RES_ACCOUNT_INFO: return parseAccountInfo(fd);
			case PACKET_REQ_CHANGE_SEX: return parseChangeSex(fd);
			case PACKET_REQ_GLOBAL_ACCREG: return parseGlobalAccountReg(fd);
			case PACKET_BAN_NOTIFICATION: return banNofitication(fd);
			case PACKET_ALREADY_ONLINE: return alreadyOnline(fd);
			case PACKET_SYNCRONIZE_IPADDRESS: return synconize(fd);
			case PACKET_REQ_VIP: return parseVip(fd);
		}

		return false;
	}

	/**
	 * Analisa a resposta de uma solicitação para se conectar com o servidor de acesso.
	 * @param fd conexão do descritor de arquivo da conexão com o servidor de acesso.
	 * @return true se deve manter a conexão ou false para encerrar.
	 */

	private boolean parseLoginResult(CFileDescriptor fd)
	{
		CharServerConnectResult packet = new CharServerConnectResult();
		packet.receive(fd, false);

		return packet.getResult() == OK;
	}

	/**
	 * Faz uma solicitação ao servidor de acesso sobre a autenticação de uma conta.
	 * Essa autenticação fica no servidor de acesso até que o de personagem solicite.
	 * Em último caso pode ser removido também devido a inatividade do jogador.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se houver conexão com o servidor de acesso ou false caso contrário.
	 */

	public boolean reqAuthAccount(CFileDescriptor fd)
	{
		if (!isConnected())
		{
			client.refuseEnter(fd, RefuseEnter.REJECTED_FROM_SERVER);
			return false;
		}

		CharSessionData sd = fd.getSessionData();

		AuthAccountRequest packet = new AuthAccountRequest();
		packet.setAccountID(sd.getID());
		packet.setFirstSeed(sd.getSeed().getFirst());
		packet.setSecondSeed(sd.getSeed().getSecond());
		packet.setIP(fd.getAddress());
		packet.setFdID(fd.getID());
		packet.send(getFileDescriptor());

		logDebug("fd#%d request auth account from login-server.\n", fd.getID());

		return true;
	}

	private boolean parseAuthAccount(CFileDescriptor lfd)
	{
		AuthAccountResult packet = new AuthAccountResult();
		packet.receive(fd);

		if (getFileDescriptorSystem().isAlive(packet.getRequestID()))
		{
			CFileDescriptor fd = (CFileDescriptor) getFileDescriptorSystem().get(packet.getRequestID());
			CharSessionData sd = fd.getSessionData();

			if (sd.isAuth() && sd.getID() == packet.getAccountID() &&
				sd.getSeed().equals(packet.getFirstSeed(), packet.getSecondSeed()))
			{
				sd.setVersion(packet.getVersion());
				sd.setClientType(packet.getClientType());

				int serverVersion = dateToVersion(PACKETVER);

				if (sd.getVersion() != serverVersion)
					logWarning("account#%d com versão %d e o servidor foi compilado para %d.\n", sd.getID(), sd.getVersion(), serverVersion);

				switch (packet.getResult())
				{
					case 0:
						return character.authOk(fd);

					case 1:
						client.refuseEnter(fd, RefuseEnter.REJECTED_FROM_SERVER);
						return false;
				}
			}
		}

		return false;
	}

	/**
	 * Faz uma solicitação ao servidor de acesso sobre os dados de uma conta.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @return true se houver conexão com o servidor de acesso ou false caso contrário.
	 */

	public boolean reqAccountData(CFileDescriptor fd)
	{
		if (!isConnected())
		{
			client.refuseEnter(fd, RefuseEnter.REJECTED_FROM_SERVER);
			return false;
		}

		CharSessionData sd = fd.getSessionData();
		AccountDataRequest packet = new AccountDataRequest();
		packet.setAccountID(sd.getID());
		packet.send(getFileDescriptor());

		logDebug("fd#%d request account data from login-server.\n", fd.getID());

		return true;
	}

	public boolean reqAccountData(int accountID)
	{
		if (!isConnected())
		{
			client.refuseEnter(fd, RefuseEnter.REJECTED_FROM_SERVER);
			return false;
		}

		AccountDataRequest packet = new AccountDataRequest();
		packet.setAccountID(accountID);
		packet.send(getFileDescriptor());

		logDebug("account#%d data request.\n", accountID, fd.getID());

		return true;
	}

	private boolean parseAccountData(CFileDescriptor fd)
	{
		// TODO chlogif_parse_reqaccdata
		return false;
	}

	private boolean keepAlive(CFileDescriptor fd)
	{
		fd.getFlag().unset(FileDescriptor.FLAG_PING);

		return fd.isConnected();
	}

	private boolean parseAccountInfo(CFileDescriptor fd)
	{
		// TODO chlogif_parse_AccInfoAck
		return false;
	}

	private boolean parseChangeSex(CFileDescriptor fd)
	{
		// TODO chlogif_parse_ackchangesex
		return false;
	}

	private boolean parseGlobalAccountReg(CFileDescriptor fd)
	{
		// TODO chlogif_parse_ack_global_accreg
		return false;
	}

	private boolean banNofitication(CFileDescriptor fd)
	{
		// TODO chlogif_parse_accbannotification
		return false;
	}

	private boolean alreadyOnline(CFileDescriptor fd)
	{
		// TODO chlogif_parse_askkick
		return false;
	}

	private boolean synconize(CFileDescriptor fd)
	{
		// TODO chlogif_parse_updip
		return false;
	}

	private boolean parseVip(CFileDescriptor fd)
	{
		// TODO chlogif_parse_vipack
		return false;
	}

	/**
	 * Envia ao servidor de acesso a quantidade de jogadores online no servidor.
	 * @param users quantidade atual de jogadores online no servidor.
	 * @return true se houver conexão com o servidor de acesso ou false caso contrário.
	 */

	public boolean updateUserCount(int users)
	{
		if (!isConnected())
		{
			client.refuseEnter(fd, RefuseEnter.REJECTED_FROM_SERVER);
			return false;
		}

		UpdateUserCount packet = new UpdateUserCount();
		packet.setCount(users);
		packet.send(getFileDescriptor());

		logDebug("update user count.\n", fd.getID());

		return true;
	}
}

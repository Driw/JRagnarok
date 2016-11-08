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
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_ACCOUNT_BAN_NOTIFICATION;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_RES_ACCOUNT_DATA;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_ACK_ACCOUNT_INFO;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_ACK_CHANGE_SEX;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_RES_AUTH_ACCOUNT;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_ACK_GLOBAL_ACCREG;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_ALREADY_ONLINE;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_PING_RESPONSE;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_PING_REQUEST;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_RES_CHAR_CONNECT;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_SYNCRONIZE_IPADDRESS;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.OK;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.REJECTED_FROM_SERVER;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.lang.IntUtil.diff;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.receive.AuthAccountReceive;
import org.diverproject.jragnarok.packets.receive.AcknowledgePacket;
import org.diverproject.jragnarok.packets.receive.CharConnectResult;
import org.diverproject.jragnarok.packets.request.CharConnectRequest;
import org.diverproject.jragnarok.packets.response.PingRequest;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.util.ObjectDescription;

public class ServiceCharLogin extends ServiceCharServer
{
	protected static final int STALL_TIME = 60;

	/**
	 * Descritor de Arquivo para com o servidor de acesso.
	 */
	private FileDescriptor fd;

	/**
	 * Serviço de comunicação entre o servidor de personagem e o cliente.
	 */
	private ServiceCharClient charClient;

	public ServiceCharLogin(CharServer server)
	{
		super(server);
	}

	/**
	 * A inicialização desse serviço consiste em adicionar dois temporizadores.
	 * O primeiro temporizador irá realizar a conexão com o servidor de acesso.
	 * Em quanto o segundo temporizador envia todas as contas acessadas.
	 */

	public void init()
	{
		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer connectionTimer = timers.acquireTimer();
		connectionTimer.setListener(checkLoginConnection);
		connectionTimer.setTick(ts.getCurrentTime() + seconds(1));
		ts.getTimers().addLoop(connectionTimer, seconds(10));

		Timer sendAccountsTimer = timers.acquireTimer();
		sendAccountsTimer.setListener(sendAccounts);
		sendAccountsTimer.setTick(ts.getCurrentTime() + seconds(1));
		ts.getTimers().addLoop(sendAccountsTimer, hours(1));

		charClient = new ServiceCharClient(getServer());
	}

	/**
	 * Terminar esse serviço inclui em fechar a conexão com o servidor de acesso.
	 * Desta forma nenhum jogador poderá se comunicar/entrar nesse servidor.
	 * Uma vez que a conexão seja fechada ela pode ser aberta novamente.
	 */

	public void destroy()
	{
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

	private final TimerListener checkLoginConnection = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			if (isConnected())
				return;

			logInfo("tentando se conectar com o servidor de personagens...\n");

			String host = getConfigs().getString(LOGIN_IP);
			short port = s(getConfigs().getInt(LOGIN_PORT));

			try {

				Socket socket = new Socket(host, port);

				fd = getFileDescriptorSystem().newFileDecriptor(socket);
				fd.setParseListener(parse);
				fd.getFlag().set(FileDescriptor.FLAG_SERVER);

				String username = getConfigs().getString(CHAR_USERNAME);
				String password = getConfigs().getString(CHAR_PASSWORD);
				int serverIP = getConfigs().getInt(CHAR_IP);
				short serverPort = s(getConfigs().getInt(CHAR_PORT));
				String serverName = getConfigs().getString(CHAR_SERVER_NAME);
				short type = s(getConfigs().getInt(CHAR_MAINTANCE));
				short newDisplay = s(getConfigs().getInt(CHAR_NEW_DISPLAY));

				CharConnectRequest packet = new CharConnectRequest();
				packet.setUsername(username);
				packet.setPassword(password);
				packet.setServerIP(serverIP);
				packet.setServerPort(serverPort);
				packet.setServerName(serverName);
				packet.setType(type);
				packet.setNewDisplay(newDisplay);
				packet.send(fd);

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
			ObjectDescription description = new ObjectDescription(getClass());

			description.append(getName());

			return description.toString();
		}
	};

	private final TimerListener sendAccounts = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public String getName()
		{
			return "send_accounts_tologin";
		}

		@Override
		public String toString()
		{
			ObjectDescription description = new ObjectDescription(getClass());

			description.append(getName());

			return description.toString();
		}
	};

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

			parsePing(fd);

			return parseCommand(fd);
		}
	};

	protected void parsePing(FileDescriptor fd2)
	{
		if (fd.getFlag().is(FileDescriptor.FLAG_PING))
		{
			if (diff(getTimerSystem().getCurrentTime(), fd.getTimeout()) > STALL_TIME * 2)
				fd.getFlag().set(FileDescriptor.FLAG_EOF);

			else if (!fd.getFlag().is(FileDescriptor.FLAG_PING_SENT))
			{
				PingRequest packet = new PingRequest();
				packet.send(fd);

				fd.getFlag().set(FileDescriptor.FLAG_PING_SENT);
			}
		}		
	}

	protected boolean parseCommand(FileDescriptor fd)
	{
		AcknowledgePacket ack = new AcknowledgePacket();
		ack.receive(fd, false);

		short command = ack.getPacketID();

		Object cache = fd.getCache();
		CharSessionData sd = (cache != null && cache instanceof CharSessionData) ? (CharSessionData) cache : null;

		switch (command)
		{
			case PACKET_RES_CHAR_CONNECT: return parseLoginResult(fd);
			case PACKET_RES_AUTH_ACCOUNT: return acknowledgeAccount(fd, sd);

			case PACKET_RES_ACCOUNT_DATA:
			case PACKET_PING_RESPONSE:
			case PACKET_PING_REQUEST:
			case PACKET_ACK_ACCOUNT_INFO:
			case PACKET_ACK_CHANGE_SEX:
			case PACKET_ACK_GLOBAL_ACCREG:
			case PACKET_ACCOUNT_BAN_NOTIFICATION:
			case PACKET_ALREADY_ONLINE:
			case PACKET_SYNCRONIZE_IPADDRESS:
		}

		return false;
	}

	private boolean parseLoginResult(FileDescriptor fd)
	{
		CharConnectResult packet = new CharConnectResult();
		packet.receive(fd, false);

		return packet.getResult() == OK;
	}

	private boolean acknowledgeAccount(FileDescriptor fd, CharSessionData sd)
	{
		AuthAccountReceive packet = new AuthAccountReceive();
		packet.receive(fd, false);

		if ((sd = (fd.getCache() == null) ? null : (CharSessionData) fd.getCache()) != null &&
			getFileDescriptorSystem().isAlive(packet.getRequestID()) &&
			!sd.isAuth() && sd.getAccountID() == packet.getAccountID() &&
			sd.getSeed().getFirst() == packet.getFirstSeed() &&
			sd.getSeed().getSecond() == packet.getSecondSeed())
		{
			sd.setVersion(packet.getVersion());
			sd.setClientType(packet.getClientType());

			int required = dateToVersion(PACKETVER);

			if (sd.getVersion() != required)
				logWarning("client desatualizado (aid: %d, version: %d, required: %d).\n", sd.getAccountID(), sd.getVersion(), required);

			fd = getFileDescriptorSystem().get(packet.getRequestID());

			switch (packet.getResult())
			{
				case AuthAccountReceive.OK:
					return charClient.charAuthOk(fd, sd);

				case AuthAccountReceive.FAILED:
					charClient.refuseEnter(fd, REJECTED_FROM_SERVER);
					return true;
			}
		}

		return false;
	}

	// TODO reset
	// TODO chlogif_parse_AccInfoAck
	// TODO chlogif_req_accinfo
	// TODO chlogif_reqvipdata
	// TODO chlogif_parse_vipack
	// TODO chlogif_parse_updip
	// TODO chlogif_parse_askkick
	// TODO chlogif_parse_accbannotification
	// TODO chlogif_parse_ack_global_accreg
	// TODO chlogif_parse_ackchangecharsex
	// TODO chlogif_parse_ackchangesex
	// TODO chlogif_parse_change_sex_sub
	// TODO chlogif_parse_keepalive
	// TODO chlogif_parse_reqaccdata
	// TODO chlogif_parse_ackaccreq
	// TODO chlogif_send_setaccoffline
	// TODO chlogif_send_setallaccoffline
	// TODO chlogif_send_setacconline
	// TODO chlogif_send_reqaccdata
	// TODO chlogif_request_accreg2
	// TODO chlogif_send_global_accreg
	// TODO chlogif_prepsend_global_accreg
	// TODO chlogif_upd_global_accreg
	// TODO chlogif_broadcast_user_count
	// TODO chlogif_send_usercount
	// TODO chlogif_send_acc_tologin
	// TODO chlogif_pincode_start
	// TODO chlogif_pincode_notifyLoginPinUpdate
	// TODO chlogif_pincode_notifyLoginPinError

}

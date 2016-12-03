package org.diverproject.jragnarok.server.character;

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
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_CHAR_SERVER_CONNECT;
import static org.diverproject.jragnarok.server.common.AuthResult.OK;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.lang.IntUtil.diff;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.receive.AcknowledgePacket;
import org.diverproject.jragnarok.packets.request.CharServerConnectRequest;
import org.diverproject.jragnarok.packets.request.CharServerConnectResult;
import org.diverproject.jragnarok.packets.response.KeepAliveRequest;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;

public class ServiceCharLogin extends ServiceCharServer
{
	protected static final int STALL_TIME = 60;

	/**
	 * Descritor de Arquivo para com o servidor de acesso.
	 */
	private FileDescriptor fd;

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

			logInfo("tentando se conectar com o servidor de acesso...\n");

			String host = getConfigs().getString(LOGIN_IP);
			short port = s(getConfigs().getInt(LOGIN_PORT));

			try {

				Socket socket = new Socket(host, port);

				fd = getFileDescriptorSystem().newFileDecriptor(socket, parse);
				fd.getFlag().set(FileDescriptor.FLAG_SERVER);

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
				KeepAliveRequest packet = new KeepAliveRequest();
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

		switch (command)
		{
			case PACKET_RES_CHAR_SERVER_CONNECT: return parseLoginResult(fd);
		}

		return false;
	}

	private boolean parseLoginResult(FileDescriptor fd)
	{
		CharServerConnectResult packet = new CharServerConnectResult();
		packet.receive(fd, false);

		return packet.getResult() == OK;
	}

	public FileDescriptor getFileDescriptor()
	{
		return fd;
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

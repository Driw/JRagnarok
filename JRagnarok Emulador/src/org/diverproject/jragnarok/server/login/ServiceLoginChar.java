package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.minutes;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP_SYNC_INTERVAL;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_KEEP_ALIVE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_UPDATE_USER_COUNT;
import static org.diverproject.log.LogSystem.logInfo;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.receive.AcknowledgePacket;
import org.diverproject.jragnarok.packets.request.UpdateUserCount;
import org.diverproject.jragnarok.packets.response.SyncronizeAddress;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;

public class ServiceLoginChar extends AbstractServiceLogin
{
	public ServiceLoginChar(LoginServer server)
	{
		super(server);
	}

	public void init()
	{
		client = getServer().getClientService();
		account = getServer().getAccountService();

		int interval = getConfigs().getInt(LOGIN_IP_SYNC_INTERVAL);

		if (interval > 0)
		{
			TimerSystem ts = getTimerSystem();
			TimerMap timers = ts.getTimers();

			Timer siaTimer = timers.acquireTimer();
			siaTimer.setListener(syncronizeIpAddress);
			siaTimer.setTick(ts.getCurrentTime() + minutes(interval));
			ts.getTimers().addLoop(siaTimer, minutes(interval));
		}
	}

	public void destroy()
	{
		// TODO Auto-generated method stub
		
	}

	private TimerListener syncronizeIpAddress = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			logInfo("Sincronização de IP em progresso...\n");

			SyncronizeAddress packet = new SyncronizeAddress();
			client.sendAllWithoutOurSelf(null, packet);
		}

		@Override
		public String getName()
		{
			return "syncronizeIpAddress";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	public final FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
				return false;

			return acknowledgePacket(fd);
		}
	};

	private boolean acknowledgePacket(FileDescriptor fd)
	{
		AcknowledgePacket packet = new AcknowledgePacket();
		packet.receive(fd, false);

		short command = packet.getPacketID();

		switch (command)
		{
			case PACKET_RES_KEEP_ALIVE:
				client.pingCharRequest(fd);
				return true;

			case PACKET_UPDATE_USER_COUNT:
				updateUserCount(fd);
				return true;

			default:
				return account.dispatch(command, fd);
		}
	}

	/**
	 * Um servidor de personagem envia a quantidade de jogadores online.
	 * Deve procurar o cliente desse servidor e atualizar a informação.
	 * @param fd conexão do servidor de personagem que está enviando.
	 */

	private void updateUserCount(FileDescriptor fd)
	{
		UpdateUserCount packet = new UpdateUserCount();
		packet.receive(fd);

		for (ClientCharServer server : getServer().getCharServerList())
			if (server.getFileDecriptor().equals(fd) && server.getUsers() != packet.getCount())
			{
				server.setUsers(s(packet.getCount()));
				logInfo("%d jogadores online em '%s'.\n", server.getUsers(), server.getName());
			}
	}
}

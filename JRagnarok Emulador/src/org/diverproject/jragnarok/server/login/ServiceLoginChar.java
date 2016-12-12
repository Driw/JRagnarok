package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.minutes;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP_SYNC_INTERVAL;
import static org.diverproject.log.LogSystem.logInfo;

import org.diverproject.jragnarok.packets.request.UpdateUserCount;
import org.diverproject.jragnarok.packets.response.SyncronizeAddress;
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

	/**
	 * Serviço para comunicação entre o servidor e o cliente.
	 */
	protected ServiceLoginClient client;

	@Override
	public void init()
	{
		client = getServer().getFacade().getClientService();

		int interval = getConfigs().getInt(LOGIN_IP_SYNC_INTERVAL);

		if (interval > 0)
		{
			TimerSystem ts = getTimerSystem();
			TimerMap timers = ts.getTimers();

			Timer siaTimer = timers.acquireTimer();
			siaTimer.setListener(SYNCRONIZE_IPADDRESS);
			siaTimer.setTick(ts.getCurrentTime() + minutes(interval));
			ts.getTimers().addLoop(siaTimer, minutes(interval));
		}
	}

	@Override
	public void destroy()
	{
		client = null;
	}

	private final TimerListener SYNCRONIZE_IPADDRESS = new TimerListener()
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

	/**
	 * Um servidor de personagem envia a quantidade de jogadores online.
	 * Deve procurar o cliente desse servidor e atualizar a informação.
	 * @param fd conexão do servidor de personagem que está enviando.
	 */

	public void updateUserCount(LFileDescriptor fd)
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

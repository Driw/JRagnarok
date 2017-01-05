package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.JRagnarokUtil.seconds;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;

public class ServiceCharServer extends AbstractCharService
{
	public static final int AUTH_TIMEOUT = seconds(30);

	/**
	 * Serviço para comunicação com o servidor de acesso.
	 */
	private ServiceCharLogin login;

	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineMap onlines;

	/**
	 * Cria uma nova instância do principal serviço para um servidor de personagem.
	 * @param server referência do servidor de personagem que irá usar o serviço.
	 */

	public ServiceCharServer(CharServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		login = getServer().getFacade().getLoginService();
		onlines = getServer().getFacade().getOnlineMap();

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer odcTimer = timers.acquireTimer();
		odcTimer.setListener(ONLINE_DATA_CLEANUP);
		odcTimer.setTick(ts.getCurrentTime() + seconds(1));
		timers.addLoop(odcTimer, seconds(10));
	}

	@Override
	public void destroy()
	{
		login = null;
		onlines = null;
	}

	private final TimerListener ONLINE_DATA_CLEANUP = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			onlines.cleanup();
		}
		
		@Override
		public String getName()
		{
			return "onlineDataCleanup";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	public final TimerListener WAITING_DISCONNECT = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public String getName()
		{
			return "waitingDisconnect";
		}
	};

	public void setCharSelect(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		OnlineCharData online = onlines.get(sd.getID());

		if (online == null)
		{
			online = new OnlineCharData();
			online.setAccountID(sd.getID());
			onlines.add(online);
		}

		MapServerList servers = getServer().getMapServers();

		if (online.getServer() > 0)
		{
			ClientMapServer server = servers.get(online.getServer());

			if (server.getUsers() > 0)
				server.setUsers(s(server.getUsers() - 1));
		}

		online.setCharID(0);
		online.setServer(0);

		if (online.getWaitingDisconnect() != null)
		{
			getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		login.setAccountOnline(sd.getID());
	}

	public void setCharOnline(int mapID, OnlineCharData online)
	{
		onlines.makeOnline(online);
	}

	public void setCharOffline(int charID, int accountID)
	{
		// TODO Auto-generated method stub
		
	}

	public int getCountUsers()
	{
		return 0;
	}

	public void disconnectPlayer(int accountID)
	{
		// TODO Auto-generated method stub
		
	}
}

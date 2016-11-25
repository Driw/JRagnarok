package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_CLEANUP_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_LIMIT;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.login.control.IpBanControl;

public class ServiceLoginIpBan extends AbstractServiceLogin
{
	private ServiceLoginLog log;
	private IpBanControl control;

	public ServiceLoginIpBan(LoginServer server)
	{
		super(server);
	}

	public void init() throws RagnarokException
	{
		log = getServer().getLogService();
		control = new IpBanControl(getConnection());

		int interval = getConfigs().getInt(IPBAN_CLEANUP_INTERVAL);

		if (interval > 0)
		{
			TimerMap timers = getTimerSystem().getTimers();

			Timer timer = timers.acquireTimer();
			timer.setTick(getTimerSystem().getCurrentTime());
			timer.setListener(cleanup);
			timers.addLoop(timer, seconds(interval));
		}
	}

	public void destroy()
	{
		control.clear();
		control = null;
	}

	private boolean isEnabled()
	{
		return getConfigs().getBool(IPBAN_ENABLED);
	}

	public boolean isBanned(int ip)
	{
		if (!isEnabled())
			return false;

		return control.addressBanned(ip);
	}

	public void addBanLog(String ip)
	{
		int minutes = getConfigs().getInt(IPBAN_PASS_FAILURE_INTERVAL);
		int limit = getConfigs().getInt(IPBAN_PASS_FAILURE_LIMIT);
		int failures = log.getFailedAttempts(ip, minutes);

		if (failures >= limit)
			;
	}

	private TimerListener cleanup = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			control.cleanup();
		}
		
		@Override
		public String getName()
		{
			return "cleanup";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};
}

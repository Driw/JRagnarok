package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_CLEANUP_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_PASS_FAILURE_LIMIT;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;

public class ServiceLoginIpBan extends AbstractServiceLogin
{
	public ServiceLoginIpBan(LoginServer server)
	{
		super(server);
	}

	public void init()
	{
		super.init();

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

	private boolean isEnabled()
	{
		return getConfigs().getBool(IPBAN_ENABLED);
	}

	public boolean isBanned(int ip)
	{
		if (!isEnabled())
			return false;

		return ipbans.addressBanned(ip);
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
			try {

				logNotice("%d listas de endereços de IP banidos expirados.\n", ipbans.cleanup());

			} catch (RagnarokException e) {
				logError("falha durante a limpeza de endereços de IP banidos expirados:\n");
				logExeception(e);
			}
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

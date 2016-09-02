package org.diverproject.jragnarok.server.config;

import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigInt;

public class ConfigIpBan
{
	private static final ConfigBoolean ENABLE_SERVICE;
	private static final ConfigInt CLEANUP_INTERVAL;
	private static final ConfigBoolean PASS_FAILURE_ENABLE;
	private static final ConfigInt PASS_FAILURE_INTERVAL;
	private static final ConfigInt PASS_FAILURE_LIMIT;
	private static final ConfigInt PASS_FAILURE_DURATION;

	static
	{
		ENABLE_SERVICE = new ConfigBoolean("ipban.enabled");
		CLEANUP_INTERVAL = new ConfigInt("ipban.cleanup_interval");
		PASS_FAILURE_ENABLE = new ConfigBoolean("ipban.pass_failure");
		PASS_FAILURE_INTERVAL = new ConfigInt("ipban.pass_failure_interval");
		PASS_FAILURE_LIMIT = new ConfigInt("ipban.pass_failure_limit");
		PASS_FAILURE_DURATION = new ConfigInt("ipban.pass_failure_duration");
	}

	public static ConfigBoolean getEnableService()
	{
		return ENABLE_SERVICE;
	}

	public static ConfigInt getCleanupinterval()
	{
		return CLEANUP_INTERVAL;
	}

	public static ConfigBoolean getPassFailureEnable()
	{
		return PASS_FAILURE_ENABLE;
	}

	public static ConfigInt getPassFailureInterval()
	{
		return PASS_FAILURE_INTERVAL;
	}

	public static ConfigInt getPassFailureLimit()
	{
		return PASS_FAILURE_LIMIT;
	}

	public static ConfigInt getPassFailureDuration()
	{
		return PASS_FAILURE_DURATION;
	}
}

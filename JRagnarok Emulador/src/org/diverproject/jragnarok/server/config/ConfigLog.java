package org.diverproject.jragnarok.server.config;

import org.diverproject.jragnaork.configuration.ConfigBoolean;

public class ConfigLog
{
	private static final ConfigBoolean LOG_LOGIN;

	static
	{
		LOG_LOGIN = new ConfigBoolean("log.login");
	}

	public static ConfigBoolean getLogLogin()
	{
		return LOG_LOGIN;
	}
}

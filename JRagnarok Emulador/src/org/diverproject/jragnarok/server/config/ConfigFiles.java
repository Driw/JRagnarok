package org.diverproject.jragnarok.server.config;

import org.diverproject.jragnaork.configuration.ConfigString;

public class ConfigFiles
{
	private static final ConfigString SQL_CONNECTION_CONFIG;
	private static final ConfigString LAN_CONFIG;
	private static final ConfigString LOG_CONFIG;
	private static final ConfigString VIP_CONFIG;
	private static final ConfigString CLIENT_CONFIG;
	private static final ConfigString IP_BAN_CONFIG;
	private static final ConfigString LOGIN_CONFIG;
	private static final ConfigString MESSAGES_CONFIG;

	static
	{
		SQL_CONNECTION_CONFIG = new ConfigString("files.");
		LAN_CONFIG = new ConfigString("files.lan_config");
		LOG_CONFIG = new ConfigString("files.log");
		VIP_CONFIG = new ConfigString("files.vip");
		CLIENT_CONFIG = new ConfigString("files.client");
		IP_BAN_CONFIG = new ConfigString("files.ipban");
		LOGIN_CONFIG = new ConfigString("files.login_config");
		MESSAGES_CONFIG = new ConfigString("files.messages_config");
	}

	public static ConfigString getSqlConnectionConfig()
	{
		return SQL_CONNECTION_CONFIG;
	}

	public static ConfigString getLanConfig()
	{
		return LAN_CONFIG;
	}

	public static ConfigString getLogConfig()
	{
		return LOG_CONFIG;
	}

	public static ConfigString getVipConfig()
	{
		return VIP_CONFIG;
	}

	public static ConfigString getClientConfig()
	{
		return CLIENT_CONFIG;
	}

	public static ConfigString getIpBanConfig()
	{
		return IP_BAN_CONFIG;
	}

	public static ConfigString getLoginConfig()
	{
		return LOGIN_CONFIG;
	}

	public static ConfigString getMessagesConfig()
	{
		return MESSAGES_CONFIG;
	}
}

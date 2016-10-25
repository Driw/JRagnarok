package org.diverproject.jragnarok.server.config;

import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigString;
import org.diverproject.jragnarok.server.config.specific.ConfigIP;

public class ConfigLogin
{
	private static final ConfigIP IP;
	private static final ConfigInt PORT;
	private static final ConfigString USERNAME;
	private static final ConfigString PASSWORD;
	private static final ConfigString DATE_FORMAT;
	private static final ConfigInt IP_SYNC_INTERVAL;
	private static final ConfigBoolean CONSOLE;
	private static final ConfigBoolean NEW_ACCOUNT_FLAG;
	private static final ConfigBoolean NEW_ACCOUNT_LENGTH_LIMIT;
	private static final ConfigBoolean USE_MD5_PASSWORD;
	private static final ConfigInt GROUP_TO_CONNECT;
	private static final ConfigInt MIN_GROUP;
	private static final ConfigInt ALLOWED_REGS;
	private static final ConfigInt TIME_ALLOWED;

	static
	{
		IP = new ConfigIP("login.ip");
		PORT = new ConfigInt("login.port");
		USERNAME = new ConfigString("login.username");
		PASSWORD = new ConfigString("login.password");
		IP_SYNC_INTERVAL = new ConfigInt("login.ip_sync_interval");
		DATE_FORMAT = new ConfigString("login.date_format");
		CONSOLE = new ConfigBoolean("login.console");
		NEW_ACCOUNT_FLAG = new ConfigBoolean("login.new_account_flag");
		NEW_ACCOUNT_LENGTH_LIMIT = new ConfigBoolean("login.new_account_length_limit");
		USE_MD5_PASSWORD = new ConfigBoolean("login.use_md5_password");
		GROUP_TO_CONNECT = new ConfigInt("login.group_to_connnect");
		MIN_GROUP = new ConfigInt("login.min_group_to_connect");
		ALLOWED_REGS = new ConfigInt("login.allowed_regs");
		TIME_ALLOWED = new ConfigInt("login.time_allowed");
	}

	public static ConfigIP getIp()
	{
		return IP;
	}

	public static ConfigInt getPort()
	{
		return PORT;
	}

	public static ConfigString getUsername()
	{
		return USERNAME;
	}

	public static ConfigString getPassword()
	{
		return PASSWORD;
	}

	public static ConfigInt getIpSyncInterval()
	{
		return IP_SYNC_INTERVAL;
	}

	public static ConfigString getDateFormat()
	{
		return DATE_FORMAT;
	}

	public static ConfigBoolean getConsole()
	{
		return CONSOLE;
	}

	public static ConfigBoolean getNewAccountFlag()
	{
		return NEW_ACCOUNT_FLAG;
	}

	public static ConfigBoolean getNewAccountLengthLimit()
	{
		return NEW_ACCOUNT_LENGTH_LIMIT;
	}

	public static ConfigBoolean getUseMD5password()
	{
		return USE_MD5_PASSWORD;
	}

	public static ConfigInt getGroupToConnect()
	{
		return GROUP_TO_CONNECT;
	}

	public static ConfigInt getMinGroup()
	{
		return MIN_GROUP;
	}

	public static ConfigInt getAllowedRegs()
	{
		return ALLOWED_REGS;
	}

	public static ConfigInt getTimeAllowed()
	{
		return TIME_ALLOWED;
	}
}

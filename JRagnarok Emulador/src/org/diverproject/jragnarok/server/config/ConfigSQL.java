package org.diverproject.jragnarok.server.config;

import org.diverproject.jragnaork.configuration.ConfigBoolean;
import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigString;

public class ConfigSQL
{
	private static final ConfigString HOST;
	private static final ConfigString USERNAME;
	private static final ConfigString PASSWORD;
	private static final ConfigString DATABASE;
	private static final ConfigInt PORT;
	private static final ConfigBoolean LEGACY_DATETIME;

	static
	{
		HOST = new ConfigString("sql.host");
		USERNAME = new ConfigString("sql.username");
		PASSWORD = new ConfigString("sql.password");
		DATABASE = new ConfigString("sql.database");
		PORT = new ConfigInt("sql.port");
		LEGACY_DATETIME = new ConfigBoolean("sql.legacydatetime");
	}

	public static ConfigString getHost()
	{
		return HOST;
	}

	public static ConfigString getUsername()
	{
		return USERNAME;
	}

	public static ConfigString getPassword()
	{
		return PASSWORD;
	}

	public static ConfigString getDatabase()
	{
		return DATABASE;
	}

	public static ConfigInt getPort()
	{
		return PORT;
	}

	public static ConfigBoolean getLegacyDatetime()
	{
		return LEGACY_DATETIME;
	}
}

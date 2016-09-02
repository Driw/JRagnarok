package org.diverproject.jragnarok.server.config;

import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigString;

public class ConfigSQL
{
	private static final ConfigString HOST;
	private static final ConfigString USERNAME;
	private static final ConfigString PASSWORD;
	private static final ConfigString DATABASE;
	private static final ConfigInt PORT;

	static
	{
		HOST = new ConfigString("sql.host");
		USERNAME = new ConfigString("sql.username");
		PASSWORD = new ConfigString("sql.password");
		DATABASE = new ConfigString("sql.database");
		PORT = new ConfigInt("sql.port");
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
}

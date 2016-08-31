package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.ConfigInt;
import org.diverproject.jragnaork.configuration.ConfigString;
import org.diverproject.jragnarok.server.ServerConfig;

public class LoginConfig extends ServerConfig
{
	@Override
	protected Config<?>[] getInitialConfigs()
	{
		return new Config<?>[]
		{
			new ConfigString("sql.host", "localhost"),
			new ConfigString("sql.username", "jragnarok"),
			new ConfigString("sql.password", "jragnarok"),
			new ConfigString("sql.database", "jragnarok"),
			new ConfigInt("sql.port", 3306),
		};
	}
}

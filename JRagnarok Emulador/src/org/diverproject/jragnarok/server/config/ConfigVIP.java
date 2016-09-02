package org.diverproject.jragnarok.server.config;

import org.diverproject.jragnaork.configuration.ConfigInt;

public class ConfigVIP
{
	private static final ConfigInt CHAR_INCREASE;
	private static final ConfigInt GROUP;

	static
	{
		CHAR_INCREASE = new ConfigInt("vip.char_increase");
		GROUP = new ConfigInt("vip.group");
	}

	public static ConfigInt getCharIncrease()
	{
		return CHAR_INCREASE;
	}

	public static ConfigInt getGroup()
	{
		return GROUP;
	}
}

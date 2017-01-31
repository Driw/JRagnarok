package org.diverproject.jragnarok.configs;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_USERNAME;
import static org.diverproject.util.Util.s;

import org.diverproject.jragnaork.configuration.Configurations;

public class MapServerConfigs extends CommonConfigs
{
	public final String ip;
	public final short port;
	public final String username;
	public final String password;

	public MapServerConfigs(Configurations configs)
	{
		super(configs);

		ip = configs.getString(MAP_IP);
		port = s(configs.getInt(MAP_PORT));
		username = configs.getString(MAP_USERNAME);
		password = configs.getString(MAP_PASSWORD);
	}
}

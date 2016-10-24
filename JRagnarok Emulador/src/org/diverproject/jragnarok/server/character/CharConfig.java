package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnarok.server.ServerConfig;
import org.diverproject.jragnarok.server.config.ConfigFiles;
import org.diverproject.jragnarok.server.config.ConfigSQL;

public class CharConfig extends ServerConfig
{
	@Override
	protected Config<?>[] getInitialConfigs()
	{
		return new Config<?>[]
		{
			ConfigFiles.getSystemConfig(),
			ConfigFiles.getSqlConnectionConfig(),
			ConfigFiles.getLanConfig(),
			ConfigFiles.getIpBanConfig(),
			ConfigFiles.getLogConfig(),
			ConfigFiles.getMessagesConfig(),
			ConfigFiles.getLoginConfig(),
			ConfigFiles.getClientConfig(),
			ConfigFiles.getVipConfig(),

			ConfigSQL.getHost(),
			ConfigSQL.getUsername(),
			ConfigSQL.getPassword(),
			ConfigSQL.getDatabase(),
			ConfigSQL.getPort(),
			ConfigSQL.getLegacyDatetime(),
		};
	}
}

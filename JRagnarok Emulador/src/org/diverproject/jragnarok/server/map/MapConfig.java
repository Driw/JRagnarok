package org.diverproject.jragnarok.server.map;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.config.ConfigFiles;
import org.diverproject.jragnarok.server.config.ConfigSQL;

public class MapConfig extends Configurations
{
	@Override
	protected Config<?>[] getInitialConfigs()
	{
		return new Config<?>[]
		{
			ConfigFiles.getSystemConfig().clone(),
			ConfigFiles.getSqlConnectionConfig().clone(),
			ConfigFiles.getLanConfig().clone(),
			ConfigFiles.getIpBanConfig().clone(),
			ConfigFiles.getLogConfig().clone(),
			ConfigFiles.getMessagesConfig().clone(),
			ConfigFiles.getLoginConfig().clone(),
			ConfigFiles.getClientConfig().clone(),
			ConfigFiles.getVipConfig().clone(),

			ConfigSQL.getHost().clone(),
			ConfigSQL.getUsername().clone(),
			ConfigSQL.getPassword().clone(),
			ConfigSQL.getDatabase().clone(),
			ConfigSQL.getPort().clone(),
			ConfigSQL.getLegacyDatetime().clone(),
		};
	}
}

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

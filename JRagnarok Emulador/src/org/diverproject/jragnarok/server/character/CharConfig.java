package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.config.ConfigChar;
import org.diverproject.jragnarok.server.config.ConfigFiles;
import org.diverproject.jragnarok.server.config.ConfigLogin;
import org.diverproject.jragnarok.server.config.ConfigSQL;

public class CharConfig extends Configurations
{
	@Override
	protected Config<?>[] getInitialConfigs()
	{
		return new Config<?>[]
		{
			ConfigFiles.getSystemConfig().clone(),
			ConfigFiles.getSqlConnectionConfig().clone(),
			ConfigFiles.getLoginConfig().clone(),
			ConfigFiles.getCharConfig().clone(),

			ConfigSQL.getHost().clone(),
			ConfigSQL.getUsername().clone(),
			ConfigSQL.getPassword().clone(),
			ConfigSQL.getDatabase().clone(),
			ConfigSQL.getPort().clone(),
			ConfigSQL.getLegacyDatetime().clone(),

			ConfigLogin.getIp().clone(),
			ConfigLogin.getPort().clone(),

			ConfigChar.getIp().clone(),
			ConfigChar.getPort().clone(),
			ConfigChar.getName().clone(),
			ConfigChar.getUsername().clone(),
			ConfigChar.getPassword().clone(),
			ConfigChar.getMaintance().clone(),
			ConfigChar.getNewDisplay().clone(),
		};
	}
}

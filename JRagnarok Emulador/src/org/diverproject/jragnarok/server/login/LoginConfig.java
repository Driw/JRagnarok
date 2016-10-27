package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnarok.server.ServerConfig;
import org.diverproject.jragnarok.server.config.ConfigClient;
import org.diverproject.jragnarok.server.config.ConfigFiles;
import org.diverproject.jragnarok.server.config.ConfigIpBan;
import org.diverproject.jragnarok.server.config.ConfigLog;
import org.diverproject.jragnarok.server.config.ConfigLogin;
import org.diverproject.jragnarok.server.config.ConfigSQL;

public class LoginConfig extends ServerConfig
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

			ConfigLogin.getIp().clone(),
			ConfigLogin.getPort().clone(),
			ConfigLogin.getUsername().clone(),
			ConfigLogin.getPassword().clone(),
			ConfigLogin.getIpSyncInterval().clone(),
			ConfigLogin.getDateFormat().clone(),
			ConfigLogin.getConsole().clone(),
			ConfigLogin.getNewAccountFlag().clone(),
			ConfigLogin.getNewAccountLengthLimit().clone(),
			ConfigLogin.getUseMD5password().clone(),
			ConfigLogin.getGroupToConnect().clone(),
			ConfigLogin.getMinGroup().clone(),
			ConfigLogin.getAllowedRegs().clone(),
			ConfigLogin.getTimeAllowed().clone(),

			ConfigIpBan.getEnableService().clone(),
			ConfigIpBan.getCleanupinterval().clone(),
			ConfigIpBan.getPassFailureEnable().clone(),
			ConfigIpBan.getPassFailureInterval().clone(),
			ConfigIpBan.getPassFailureLimit().clone(),
			ConfigIpBan.getPassFailureDuration().clone(),

			ConfigLog.getLogLogin().clone(),

			ConfigClient.getCheckVersion().clone(),
			ConfigClient.getVersion().clone(),
			ConfigClient.getHashCheck().clone(),
			ConfigClient.getHashNodes().clone(),
			ConfigClient.getCharPerAccount().clone(),
		};
	}
}

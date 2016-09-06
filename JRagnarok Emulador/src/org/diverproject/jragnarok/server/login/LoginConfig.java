package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnaork.configuration.Config;
import org.diverproject.jragnarok.server.ServerConfig;
import org.diverproject.jragnarok.server.config.ConfigClient;
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
			ConfigSQL.getHost(),
			ConfigSQL.getUsername(),
			ConfigSQL.getPassword(),
			ConfigSQL.getDatabase(),
			ConfigSQL.getPort(),

			ConfigLogin.getIp(),
			ConfigLogin.getPort(),
			ConfigLogin.getUsername(),
			ConfigLogin.getPassword(),
			ConfigLogin.getIpSyncInterval(),
			ConfigLogin.getDateFormat(),
			ConfigLogin.getConsole(),
			ConfigLogin.getNewAccountFlag(),
			ConfigLogin.getNewAccountLengthLimit(),
			ConfigLogin.getUseMD5password(),
			ConfigLogin.getGroupToConnect(),
			ConfigLogin.getMinGroup(),
			ConfigLogin.getAllowedRegs(),
			ConfigLogin.getTimeAllowed(),

			ConfigIpBan.getEnableService(),
			ConfigIpBan.getCleanupinterval(),
			ConfigIpBan.getPassFailureEnable(),
			ConfigIpBan.getPassFailureInterval(),
			ConfigIpBan.getPassFailureLimit(),
			ConfigIpBan.getPassFailureDuration(),

			ConfigLog.getLogLogin(),

			ConfigClient.getCheckVersion(),
			ConfigClient.getVersion(),
			ConfigClient.getHashCheck(),
			ConfigClient.getHashNodes(),
			ConfigClient.getCharPerAccount(),
		};
	}
}

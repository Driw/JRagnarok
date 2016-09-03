package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.Client;
import org.diverproject.jragnarok.server.ClientPlayer;
import org.diverproject.jragnarok.server.login.LoginServer;

public class LoginLogService extends LoginServerService
{
	private ControllerLoginLog controller;

	public LoginLogService(LoginServer server) throws RagnarokException
	{
		super(server);
	}

	public void init()
	{
		try {
			controller = new ControllerLoginLog(getConnection());
		} catch (RagnarokException e) {
			logWarning("inicie ou reinicie o servidor, sem conexão MySQL.\n");
			logExeception(e);
		}
	}

	public void addLoginLog(Client client, int rcode, String message)
	{
		if (!client.isConnected())
			return;

		try {
			controller.log(client, rcode, message);
		} catch (RagnarokException e) {
			logExeception(e);
		}
	}

	public int getFailedAttempts(ClientPlayer player, int minutes)
	{
		if (!player.isConnected())
			return 0;

		int failures = 0;

		try {
			failures = controller.countFailedAttempts(player, minutes);
		} catch (RagnarokException e) {
			logExeception(e);
		}

		return failures;
	}
}

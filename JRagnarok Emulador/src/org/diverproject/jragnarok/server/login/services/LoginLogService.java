package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.login.entities.LoginLog;
import org.diverproject.jragnarok.server.login.structures.Login;
import org.diverproject.util.SocketUtil;

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
			logWarning("inicie ou reinicie o servidor, sem conex�o MySQL.\n");
			logExeception(e);
		}
	}

	public void addLoginLog(String ip, Login login, int code, String message)
	{
		try {

			LoginLog log = new LoginLog();
			log.getTime().set(System.currentTimeMillis());
			log.getIP().set(SocketUtil.socketIPInt(ip));
			log.setUser(login.getUsername());
			log.setRCode(code);
			log.setMessage(message);

			if (!controller.insert(log))
				logWarning("falha ao registrar log (ip: %s, username: %s)", ip, login.getUsername());

		} catch (RagnarokException e) {
			logExeception(e);
		}
	}

	public int getFailedAttempts(String ip, int minutes)
	{
		int failures = 0;

		try {
			failures = controller.countFailedAttempts(ip, minutes);
		} catch (RagnarokException e) {
			logExeception(e);
		}

		return failures;
	}
}

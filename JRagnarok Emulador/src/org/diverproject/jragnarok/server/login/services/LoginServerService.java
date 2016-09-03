package org.diverproject.jragnarok.server.login.services;

import org.diverproject.jragnarok.server.ServerService;
import org.diverproject.jragnarok.server.login.LoginServer;

public class LoginServerService extends ServerService
{
	public LoginServerService(LoginServer server)
	{
		super(server);
	}

	@Override
	protected LoginServer getServer()
	{
		return (LoginServer) super.getServer();
	}
}

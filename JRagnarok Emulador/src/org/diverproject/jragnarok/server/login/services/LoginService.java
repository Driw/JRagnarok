package org.diverproject.jragnarok.server.login.services;

import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;

public class LoginService extends LoginServerService
{
	public LoginService(LoginServer server)
	{
		super(server);
	}

	public AuthResult mmoAuth(LoginSessionData sd, boolean server)
	{
		return AuthResult.OK;
	}
}

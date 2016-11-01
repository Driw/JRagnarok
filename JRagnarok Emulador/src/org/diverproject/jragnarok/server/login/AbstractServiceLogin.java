package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnarok.server.ServerService;

public class AbstractServiceLogin extends ServerService
{
	public AbstractServiceLogin(LoginServer server)
	{
		super(server);
	}

	@Override
	protected LoginServer getServer()
	{
		return (LoginServer) super.getServer();
	}
}

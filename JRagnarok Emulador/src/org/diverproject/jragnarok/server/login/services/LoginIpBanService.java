package org.diverproject.jragnarok.server.login.services;

import org.diverproject.jragnarok.server.login.LoginServer;

public class LoginIpBanService extends LoginServerService
{
	public LoginIpBanService(LoginServer server)
	{
		super(server);
	}

	public boolean isBanned(String ip)
	{
		return false;
	}

	public void addBanLog(String ip)
	{
		
	}

//	cleanup

	public void init()
	{
		// TODO Auto-generated method stub
		
	}

//	shutdown
}

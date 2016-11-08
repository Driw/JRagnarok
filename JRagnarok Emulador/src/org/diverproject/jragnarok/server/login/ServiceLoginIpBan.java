package org.diverproject.jragnarok.server.login;

public class ServiceLoginIpBan extends AbstractServiceLogin
{
	public ServiceLoginIpBan(LoginServer server)
	{
		super(server);
	}

	public boolean isBanned(String ip)
	{
		return false;
	}

	public void add(String ip)
	{
		
	}

//	cleanup

	public void init()
	{
		// TODO Auto-generated method stub
		
	}

//	shutdown
}

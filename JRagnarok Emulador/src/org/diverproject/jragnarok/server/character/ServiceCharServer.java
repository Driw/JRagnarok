package org.diverproject.jragnarok.server.character;

public class ServiceCharServer extends AbstractCharService
{
	public ServiceCharServer(CharServer server)
	{
		super(server);
	}

	@Override
	protected CharServer getServer()
	{
		return (CharServer) super.getServer();
	}

	public boolean authOk(CFileDescriptor fd)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public int getCountUsers()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}

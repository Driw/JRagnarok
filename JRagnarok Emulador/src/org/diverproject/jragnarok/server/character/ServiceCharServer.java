package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.server.ServerService;

public class ServiceCharServer extends ServerService
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
}

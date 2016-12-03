package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.server.ServerService;

public class AbstractCharService extends ServerService
{
	public AbstractCharService(CharServer server)
	{
		super(server);
	}

	@Override
	protected CharServer getServer()
	{
		return (CharServer) super.getServer();
	}
}

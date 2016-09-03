package org.diverproject.jragnarok.server;

import java.sql.Connection;

import org.diverproject.util.ObjectDescription;

public class ServerService
{
	private Server server;

	public ServerService(Server server)
	{
		this.server = server;
	}

	protected Server getServer()
	{
		return server;
	}

	protected final ServerConfig getConfigs()
	{
		return server.getConfigs();
	}

	protected final Connection getConnection()
	{
		return server.getMySQL().getConnection();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("thread", server.getThreadName());

		return description.toString();
	}
}

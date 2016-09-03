package org.diverproject.jragnarok.server;

import java.sql.Connection;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.sql.MySQL;

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

	protected final Connection getConnection() throws RagnarokException
	{
		if (server == null)
			throw new RagnarokException("serviço sem servidor");

		MySQL mysql = server.getMySQL();

		if (mysql == null)
			throw new RagnarokException("sem conexão com o banco de dados");

		Connection connection = mysql.getConnection();

		try {
			if (connection.isClosed())
				throw new RagnarokException("conexão fecahda");
		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		return connection;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("thread", server.getThreadName());

		return description.toString();
	}
}

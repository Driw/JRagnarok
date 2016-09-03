package org.diverproject.jragnarok.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;

public class AbstractController
{
	protected Connection connection;

	public AbstractController(Connection connection)
	{
		this.connection = connection;
	}

	protected Connection getConnection() throws RagnarokException
	{
		if (connection == null)
			throw new RagnarokException("conexão não definida");

		try {

			if (connection.isClosed())
				throw new RagnarokException("conexão fechada");

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		return connection;
	}

	protected PreparedStatement prepare(String sql) throws RagnarokException, SQLException
	{
		return getConnection().prepareStatement(sql);
	}
}

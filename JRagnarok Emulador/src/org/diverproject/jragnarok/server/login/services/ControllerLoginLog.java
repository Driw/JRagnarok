package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.log.LogSystem.logWarning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractController;
import org.diverproject.jragnarok.server.Client;
import org.diverproject.jragnarok.server.Tables;

public class ControllerLoginLog extends AbstractController
{
	public ControllerLoginLog(Connection connection)
	{
		super(connection);
	}

	public int countFailedAttempts(Client client, int minutes) throws RagnarokException
	{
		String sql = "SELECT COUNT(*) AS count FROM ? WHERE ip = ?"
					+" AND (rcode = 0 OR rcode = 1) AND time > (NOW() - INTERVAL ? MINUTE)";

		Tables tables = Tables.getInstance();
		String table = tables.getLoginLog();

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, table);
			ps.setString(2, client.getIP());
			ps.setInt(3, minutes);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return rs.getInt("count");

			throw new RagnarokException("resultado não encontrado");

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	public void log(Client client, int rcode, String message) throws RagnarokException
	{
		String sql = "INSERT INTO ? (time, ip, user, rcode, log) VALUES (NOEW(), ?, ?, ?, ?)";

		Tables tables = Tables.getInstance();
		String table = tables.getLoginLog();

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, table);
			ps.setString(2, client.getIP());
			ps.setString(3, client.getUsername());
			ps.setInt(4, rcode);
			ps.setString(5, message);

			if (ps.executeUpdate() != 1)
				logWarning("falha no log de um login (ip: %s, user: %s)", client.getIP(), client.getUsername());

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

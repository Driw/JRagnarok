package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.log.LogSystem.logWarning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractController;
import org.diverproject.jragnarok.server.Tables;

public class ControllerLoginLog extends AbstractController
{
	public ControllerLoginLog(Connection connection)
	{
		super(connection);
	}

	public int countFailedAttempts(String ip, int minutes) throws RagnarokException
	{
		String sql = "SELECT COUNT(*) AS count FROM ? WHERE ip = ?"
					+" AND (rcode = 0 OR rcode = 1) AND time > (NOW() - INTERVAL ? MINUTE)";

		Tables tables = Tables.getInstance();
		String table = tables.getLoginLog();

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, table);
			ps.setString(2, ip);
			ps.setInt(3, minutes);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return rs.getInt("count");

			throw new RagnarokException("resultado não encontrado");

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	public boolean add(LoginLog log) throws RagnarokException
	{
		String sql = "INSERT INTO ? (time, ip, user, rcode, message) VALUES (NOW(), ?, ?, ?, ?)";

		Tables tables = Tables.getInstance();
		String table = tables.getLoginLog();

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, table);
			ps.setDate(2, log.getTime().toDateSQL());
			ps.setString(3, log.getIP().getString());
			ps.setInt(4, log.getRCode());
			ps.setString(5, log.getMessage());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

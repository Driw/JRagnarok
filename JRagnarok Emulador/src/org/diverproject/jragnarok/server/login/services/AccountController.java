package org.diverproject.jragnarok.server.login.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractController;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.util.Time;

public class AccountController extends AbstractController
{
	public AccountController(Connection connection)
	{
		super(connection);
	}

	public Time getBanTime(String username) throws RagnarokException
	{
		String sql = "SELECT unban_time FROM ? WHERE username = ?";

		Tables tables = Tables.getInstance();
		String table = tables.getLogin();

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, table);
			ps.setString(2, username);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				long unban_time = rs.getLong("unban_time");
				Time time = new Time(unban_time);

				return time;
			}

			throw new RagnarokException("usuário '%s' não encontrado", username);

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractDAO;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.Pincode;

public class PincodeDAO extends AbstractDAO
{
	public PincodeDAO(Connection connection)
	{
		super(connection);
	}

	public void load(Pincode pincode) throws RagnarokException
	{
		String table = Tables.getInstance().getPincodes();
		String sql = format("SELECT enabled, code, changed FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, pincode.getID());

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				throw new RagnarokException("pincode '%d' não encontrado", pincode.getID());

			pincode.setEnabled(rs.getBoolean("enabled"));
			pincode.setCode(rs.getString("code"));
			pincode.getChanged().set(rs.getDate("changed").getTime());

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}
}

package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractDAO;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountGroup;
import org.diverproject.jragnarok.server.login.entities.AccountState;
import org.diverproject.jragnarok.server.login.entities.Group;
import org.diverproject.jragnarok.server.login.entities.GroupCommand;
import org.diverproject.jragnarok.server.login.entities.GroupCommands;
import org.diverproject.jragnarok.server.login.entities.GroupPermission;
import org.diverproject.jragnarok.server.login.entities.GroupPermissions;
import org.diverproject.jragnarok.server.login.entities.Pincode;
import org.diverproject.jragnarok.server.login.entities.Vip;

public class AccountDAO extends AbstractDAO
{
	public AccountDAO(Connection connection)
	{
		super(connection);
	}

	public Account select(String username) throws RagnarokException
	{
		String accountTable = Tables.getInstance().getAccounts();
		String loginTable = Tables.getInstance().getLogin();

		String sql = format("SELECT id, password, last_login, registered,"
						+ " email, birth_date, login_count, unban, expiration, pincode, groupid, state, last_ip"
						+ " FROM %s INNER JOIN %s ON %s.id = %s.login"
						+ " WHERE username = ?",
						accountTable, loginTable, accountTable);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				Account account = new Account();
				account.getLogin().setID(rs.getInt("id"));
				account.getLogin().setUsername(username);
				account.getLogin().setPassword(rs.getString("password"));
				account.getLogin().getRegistered().set(rs.getDate("registered").getTime());
				account.getLogin().getLastLogin().set(rs.getDate("last_login").getTime());
				account.setEmail(rs.getString("email"));
				account.setBirthDate(rs.getString("birth_date"));
				account.setLoginCount(rs.getInt("login_count"));
				account.getUnban().set(rs.getDate("unban").getTime());
				account.getExpiration().set(rs.getDate("expiration").getTime());
				account.setState(AccountState.parse(rs.getInt("state")));
				account.getLastIP().set(rs.getInt("last_ip"));

				loadPincode(account.getPincode(), rs.getInt("pincode"));
				loadAccountGroup(account.getGroup(), rs.getInt("groupid"));

				return account;
			}

			throw new RagnarokException("usu�rio '%s' n�o encontrado");

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	private void loadPincode(Pincode pincode, int pincodeID) throws RagnarokException
	{
		String table = Tables.getInstance().getPincodes();
		String sql = format("SELECT enabled, code, changed FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, pincodeID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				pincode.setID(pincodeID);
				pincode.setEnabled(rs.getBoolean("enabled"));
				pincode.setCode(rs.getString("code"));
				pincode.getChanged().set(rs.getDate("changed").getTime());

				return;
			}

			throw new RagnarokException("pincode '%d' n�o encontrado", pincodeID);

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	private void loadAccountGroup(AccountGroup accountGroup, int groupID) throws RagnarokException
	{
		String table = Tables.getInstance().getAccountsGroup();
		String sql = format("SELECT current_group, old_group, vip, timeout FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, groupID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				accountGroup.setID(groupID);
				accountGroup.getTime().set(rs.getDate("timeout").getTime());

				loadGroup(accountGroup.getCurrentGroup(), rs.getInt("current_group"));
				loadGroup(accountGroup.getOldGroup(), rs.getInt("old_group"));
				loadVip(accountGroup.getVip(), rs.getInt("vip"));

				return;
			}

			throw new RagnarokException("grupo de conta '%d' n�o encontrado", groupID);

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	private void loadGroup(Group group, int groupID) throws RagnarokException
	{
		String table = Tables.getInstance().getGroups();
		String sql = format("SELECT level, name, parent, log_enabled FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, groupID);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				group.setID(groupID);
				group.setLevel(rs.getInt("level"));
				group.setName(rs.getString("name"));
				group.setLogEnabled(rs.getBoolean("log_enabled"));

				int parent = rs.getInt("parent");
				{
					group.setParent(new Group());
					loadGroup(group.getParent(), parent);
				}

				loadGroupPermissions(group);
				loadGroupCommands(group);

				return;
			}

			throw new RagnarokException("grupo de conta '%d' n�o encontrado", groupID);

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	private void loadGroupPermissions(Group group) throws RagnarokException
	{
		String table = Tables.getInstance().getGroupPermissionsList();
		String permissionsTable = Tables.getInstance().getGroupPermissions();

		String sql = format("SELECT id, name FROM %s"
						+ " INNER JOIN %s ON %s.id = %s.permission"
						+ " WHERE groupid = ?",
						table, permissionsTable, permissionsTable, table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, group.getID());

			ResultSet rs = ps.executeQuery();
			GroupPermissions permissions = group.getPermissions();

			while (rs.next())
			{
				GroupPermission permission = new GroupPermission();
				permission.setID(rs.getInt("id"));
				permission.setName(rs.getString("name"));
				permissions.add(permission);
			}

		} catch (SQLException e) {
			new RagnarokException(e);
		}
	}

	private void loadGroupCommands(Group group) throws RagnarokException
	{
		String table = Tables.getInstance().getGroupPermissionsList();
		String commandsTable = Tables.getInstance().getGroupCommands();

		String sql = format("SELECT id, name FROM %s"
						+ " INNER JOIN %s ON %s.id = %s.command"
						+ " WHERE groupid = ?",
						table, commandsTable, commandsTable, table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, group.getID());

			ResultSet rs = ps.executeQuery();
			GroupCommands commands = group.getCommands();

			while (rs.next())
			{
				GroupCommand command = new GroupCommand();
				command.setID(rs.getInt("id"));
				command.setName(rs.getString("name"));
				commands.add(command);
			}

		} catch (SQLException e) {
			new RagnarokException(e);
		}		
	}

	private void loadVip(Vip vip, int vipID) throws RagnarokException
	{
		if (vipID == 0)
			return;

		String table = Tables.getInstance().getVip();
		String sql = format("name, groupid, char_slot_count, max_storage FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, vip.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				vip.setID(vipID);
				vip.setCharSlotCount(rs.getByte("char_slot_count"));
				vip.setMaxStorage(rs.getShort("max_storage"));

				loadGroup(vip.getGroup(), rs.getInt("groupid"));
			}

		} catch (SQLException e) {
			new RagnarokException(e);
		}	
	}
}
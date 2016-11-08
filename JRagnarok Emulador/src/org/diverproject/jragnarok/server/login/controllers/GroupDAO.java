package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractDAO;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.AccountGroup;
import org.diverproject.jragnarok.server.login.entities.Group;
import org.diverproject.jragnarok.server.login.entities.GroupCommand;
import org.diverproject.jragnarok.server.login.entities.GroupCommands;
import org.diverproject.jragnarok.server.login.entities.GroupPermission;
import org.diverproject.jragnarok.server.login.entities.GroupPermissions;
import org.diverproject.jragnarok.server.login.entities.Vip;

/**
 * <h1>DAO para Grupo de Jogadores</h1>
 *
 * <p>Realiza a comunica��o do sistema com o banco de dados para obter informa��es de grupo dos jogadores.
 * Atrav�s de um determinado objeto passado � poss�vel que os dados de um grupo sejam carregados.
 * Para isso ser� necess�rio apenas que o c�digo de identifica��o do mesmo tenha sido definido.</p>
 *
 * @see AbstractDAO
 * @see AccountGroup
 * @see Group
 * @see GroupCommand
 * @see GroupPermission
 * @see Vip
 *
 * @author Andrew
 */

public class GroupDAO extends AbstractDAO
{
	/**
	 * Cria uma nova DAO para trabalhar com grupo de jogadores do banco de dados.
	 * @param connection refer�ncia da conex�o com o banco de dados a considerar.
	 */

	public GroupDAO(Connection connection)
	{
		super(connection);
	}

	/**
	 * Carrega todas as informa��es necess�rios do grupo de uma determinada conta.
	 * Necess�rio ter definido o c�digo de identifica��o do grupo com a conta.
	 * @param accountGroup refer�ncia do grupo vinculado a uma conta carregada.
	 * @throws RagnarokException se houver falha com a conex�o.
	 */

	public void load(AccountGroup accountGroup) throws RagnarokException
	{
		String table = Tables.getInstance().getAccountsGroup();
		String sql = format("SELECT current_group, old_group, vip, timeout FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, accountGroup.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				accountGroup.getTime().set(rs.getDate("timeout").getTime());

				int currentGroup = rs.getInt("current_group");
				int oldGroup = rs.getInt("old_group");
				int vip = rs.getInt("vip");

				if (currentGroup > 0)
				{
					accountGroup.setCurrentGroup(new Group());
					accountGroup.getCurrentGroup().setID(currentGroup);
					loadGroup(accountGroup.getCurrentGroup());
				}

				if (oldGroup > 0)
				{
					accountGroup.setOldGroup(new Group());
					accountGroup.getOldGroup().setID(oldGroup);
					loadGroup(accountGroup.getOldGroup());
				}

				if (vip > 0)
				{
					accountGroup.setVip(new Vip());
					accountGroup.getVip().setID(vip);
					loadVip(accountGroup.getVip());
				}

				return;
			}

			throw new RagnarokException("grupo de conta '%d' n�o encontrado", accountGroup.getID());

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite carregar um determinado grupo com as informa��es do banco de dados.
	 * Necess�rio ter definido o c�digo de identifica��o do grupo de jogadores.
	 * @param group refer�ncia do grupo que ser� carregado do banco de dados.
	 * @throws RagnarokException se houver falha com a conex�o.
	 */

	private void loadGroup(Group group) throws RagnarokException
	{
		String table = Tables.getInstance().getGroups();
		String sql = format("SELECT level, name, parent, log_enabled FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, group.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				group.setLevel(rs.getInt("level"));
				group.setName(rs.getString("name"));
				group.setLogEnabled(rs.getBoolean("log_enabled"));

				int parent = rs.getInt("parent");

				if (parent > 0)
				{
					group.setParent(new Group());
					group.getParent().setID(parent);
					loadGroup(group.getParent());
				}

				loadGroupPermissions(group);
				loadGroupCommands(group);

				return;
			}

			throw new RagnarokException("grupo de conta '%d' n�o encontrado", group.getID());

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite carregar as permiss�es de um grupo com as informa��es do banco de dados.
	 * Necess�rio ter definido o c�digo de identifica��o do grupo de jogadores.
	 * @param group refer�ncia do grupo que ser� carregado do banco de dados.
	 * @throws RagnarokException se houver falha com a conex�o.
	 */

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
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite carregar os comandos de um grupo com as informa��es do banco de dados.
	 * Necess�rio ter definido o c�digo de identifica��o do grupo de jogadores.
	 * @param group refer�ncia do grupo que ser� carregado do banco de dados.
	 * @throws RagnarokException se houver falha com a conex�o.
	 */

	private void loadGroupCommands(Group group) throws RagnarokException
	{
		String table = Tables.getInstance().getGroupCommandsList();
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
			throw new RagnarokException(e);
		}		
	}

	/**
	 * Permite carregar um determinado vip com as informa��es do banco de dados.
	 * Necess�rio ter definido o c�digo de identifica��o do acesso vip.
	 * @param vip refer�ncia do acesso vip que ser� carregado do banco de dados.
	 * @throws RagnarokException se houver falha com a conex�o.
	 */

	private void loadVip(Vip vip) throws RagnarokException
	{
		String table = Tables.getInstance().getVip();
		String sql = format("name, groupid, char_slot_count, max_storage FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, vip.getID());

			ResultSet rs = ps.executeQuery();

			if (rs.next())
			{
				vip.setCharSlotCount(rs.getByte("char_slot_count"));
				vip.setMaxStorage(rs.getShort("max_storage"));
				vip.getGroup().setID(rs.getInt("groupid"));

				loadGroup(vip.getGroup());
			}

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}	
	}
}

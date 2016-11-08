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
 * <p>Realiza a comunicação do sistema com o banco de dados para obter informações de grupo dos jogadores.
 * Através de um determinado objeto passado é possível que os dados de um grupo sejam carregados.
 * Para isso será necessário apenas que o código de identificação do mesmo tenha sido definido.</p>
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
	 * @param connection referência da conexão com o banco de dados a considerar.
	 */

	public GroupDAO(Connection connection)
	{
		super(connection);
	}

	/**
	 * Carrega todas as informações necessários do grupo de uma determinada conta.
	 * Necessário ter definido o código de identificação do grupo com a conta.
	 * @param accountGroup referência do grupo vinculado a uma conta carregada.
	 * @throws RagnarokException se houver falha com a conexão.
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

			throw new RagnarokException("grupo de conta '%d' não encontrado", accountGroup.getID());

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite carregar um determinado grupo com as informações do banco de dados.
	 * Necessário ter definido o código de identificação do grupo de jogadores.
	 * @param group referência do grupo que será carregado do banco de dados.
	 * @throws RagnarokException se houver falha com a conexão.
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

			throw new RagnarokException("grupo de conta '%d' não encontrado", group.getID());

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite carregar as permissões de um grupo com as informações do banco de dados.
	 * Necessário ter definido o código de identificação do grupo de jogadores.
	 * @param group referência do grupo que será carregado do banco de dados.
	 * @throws RagnarokException se houver falha com a conexão.
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
	 * Permite carregar os comandos de um grupo com as informações do banco de dados.
	 * Necessário ter definido o código de identificação do grupo de jogadores.
	 * @param group referência do grupo que será carregado do banco de dados.
	 * @throws RagnarokException se houver falha com a conexão.
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
	 * Permite carregar um determinado vip com as informações do banco de dados.
	 * Necessário ter definido o código de identificação do acesso vip.
	 * @param vip referência do acesso vip que será carregado do banco de dados.
	 * @throws RagnarokException se houver falha com a conexão.
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

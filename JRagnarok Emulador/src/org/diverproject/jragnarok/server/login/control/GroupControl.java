package org.diverproject.jragnarok.server.login.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.AccountGroup;
import org.diverproject.jragnarok.server.login.entities.Group;
import org.diverproject.jragnarok.server.login.entities.GroupCommand;
import org.diverproject.jragnarok.server.login.entities.GroupCommands;
import org.diverproject.jragnarok.server.login.entities.GroupPermission;
import org.diverproject.jragnarok.server.login.entities.GroupPermissions;
import org.diverproject.jragnarok.server.login.entities.Vip;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Controle para Grupo de Jogadores</h1>
 *
 * <p>Realiza a comunica��o do sistema com o banco de dados para obter informa��es de grupo dos jogadores.
 * Atrav�s de um determinado objeto passado � poss�vel que os dados de um grupo sejam carregados.
 * Para isso ser� necess�rio apenas que o c�digo de identifica��o do mesmo tenha sido definido.</p>
 *
 * @see AbstractControl
 * @see Group
 * @see GroupCommand
 * @see GroupPermission
 * @see Vip
 *
 * @author Andrew
 */

public class GroupControl extends AbstractControl
{
	/**
	 * Mapeamento dos grupos de jogadores j� carregados.
	 */
	private IntegerLittleMap<Group> groups;

	/**
	 * Mapeamento dos tipos de acesso vip j� carregados.
	 */
	private IntegerLittleMap<Vip> vips;

	/**
	 * Cria uma nova DAO para trabalhar com grupo de jogadores do banco de dados.
	 * @param connection refer�ncia da conex�o com o banco de dados a considerar.
	 */

	public GroupControl(Connection connection)
	{
		super(connection);

		groups = new IntegerLittleMap<>();
		vips = new IntegerLittleMap<>();
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

			if (!rs.next())
				throw new RagnarokException("grupo de conta '%d' n�o encontrado", accountGroup.getID());

			accountGroup.getTime().set(rs.getDate("timeout").getTime());

			int currentGroupID = rs.getInt("current_group");
			int oldGroupID = rs.getInt("old_group");
			int vipID = rs.getInt("vip");

			if (currentGroupID > 0)
			{
				Group group = groups.get(currentGroupID);

				if (group == null)
					group = get(currentGroupID);

				accountGroup.setCurrentGroup(group);
			}

			if (oldGroupID > 0)
			{
				Group group = groups.get(currentGroupID);

				if (group == null)
					group = get(oldGroupID);

				accountGroup.setOldGroup(group);
			}

			if (vipID > 0)
			{
				Vip vip = vips.get(vipID);

				if (vip == null)
					vip = getVip(vipID);

				accountGroup.setVip(vip);
			}

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite obter das informa��es de um determinado grupo do banco de dados.
	 * @param int groupID c�digo de identifica��o do grupo do qual deseja.
	 * @throws RagnarokException falha de conex�o ou inconsist�ncia nos dados.
	 */

	public Group get(int groupID) throws RagnarokException
	{
		Group group = groups.get(groupID);

		if (group != null)
			return group;

		String table = Tables.getInstance().getGroups();
		String sql = format("SELECT level, name, parent, log_enabled FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, groupID);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				throw new RagnarokException("grupo de conta '%d' n�o encontrado", groupID);

			group = new Group();
			group.setLevel(rs.getInt("level"));
			group.setName(rs.getString("name"));
			group.setLogEnabled(rs.getBoolean("log_enabled"));

			int parentID = rs.getInt("parent");

			if (parentID > 0)
			{
				Group parent = get(parentID);
				group.setParent(parent);
			}

			loadGroupPermissions(group);
			loadGroupCommands(group);

			groups.add(group.getID(), group);

			return group;

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
	 * Permite obter das informa��es de um determinado grupo do banco de dados.
	 * @param int vipID c�digo de identifica��o do grupo do qual deseja.
	 * @throws RagnarokException falha de conex�o ou inconsist�ncia nos dados.
	 */

	public Vip getVip(int vipID) throws RagnarokException
	{
		Vip vip = vips.get(vipID);

		if (vip != null)
			return vip;

		String table = Tables.getInstance().getVip();
		String sql = format("name, groupid, char_slot_count, max_storage FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, vipID);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				throw new RagnarokException("acesso vip '%d' n�o encontrado", vipID);

			vip = new Vip();
			vip.setID(vipID);
			vip.setCharSlotCount(rs.getByte("char_slot_count"));
			vip.setMaxStorage(rs.getShort("max_storage"));
			vip.getGroup().setID(rs.getInt("groupid"));
			vips.add(vipID, vip);

			return vip;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}	
	}

	/**
	 * Realiza uma limpeza no cache que mant�m dados de grupos e acessos vip carregados.
	 */

	public void clear()
	{
		groups.clear();
		vips.clear();
	}
}

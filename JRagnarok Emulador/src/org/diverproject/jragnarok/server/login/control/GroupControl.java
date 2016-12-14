package org.diverproject.jragnarok.server.login.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountGroup;
import org.diverproject.jragnarok.server.login.entities.Group;
import org.diverproject.jragnarok.server.login.entities.GroupCommands;
import org.diverproject.jragnarok.server.login.entities.GroupPermissions;
import org.diverproject.jragnarok.server.login.entities.Vip;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Controle para Grupo de Jogadores</h1>
 *
 * <p>Realiza a comunicação do sistema com o banco de dados para obter informações de grupo dos jogadores.
 * Através de um determinado objeto passado é possível que os dados de um grupo sejam carregados.
 * Para isso será necessário apenas que o código de identificação do mesmo tenha sido definido.</p>
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
	 * Mapeamento dos grupos de jogadores já carregados.
	 */
	private IntegerLittleMap<Group> groups;

	/**
	 * Mapeamento dos tipos de acesso vip já carregados.
	 */
	private IntegerLittleMap<Vip> vips;

	/**
	 * Cria uma nova DAO para trabalhar com grupo de jogadores do banco de dados.
	 * @param connection referência da conexão com o banco de dados a considerar.
	 */

	public GroupControl(Connection connection)
	{
		super(connection);
	}

	/**
	 * Procedimento interno usado para se criar um novo objeto com informações de um grupo de conta.
	 * @param rs resultado obtido da consulta no banco de dados contendo os dados do grupo.
	 * @return aquisição de um grupo de conta criado a partir das informações da consulta passada.
	 * @throws SQLException apenas se houver falha de conexão com o banco de dados.
	 * @throws RagnarokException durante o carregamento das permissões e comandos do grupo.
	 */

	private Group newGroup(ResultSet rs) throws SQLException, RagnarokException
	{
		Group group = new Group();
		group.setID(rs.getInt("id"));
		group.setAccessLevel(rs.getInt("access_level"));
		group.setName(rs.getString("name"));
		group.setLogCommands(rs.getBoolean("log_commands"));

		int parentID = rs.getInt("parent");

		if (parentID > 0)
		{
			Group parent = getGroup(parentID);
			group.setParent(parent);
		}

		loadGroupPermissions(group);
		loadGroupCommands(group);

		return group;
	}

	/**
	 * Procedimento interno usado para se criar um novo objeto com informações de um vip.
	 * @param rs resultado obtido da consulta no banco de dados contendo os dados do vip.
	 * @return aquisição de um vip criado a partir das informações da consulta passada.
	 * @throws SQLException apenas se houver falha de conexão com o banco de dados.
	 * @throws RagnarokException apenas se houver falha de conexão com o banco de dados.
	 */

	private Vip newVip(ResultSet rs) throws SQLException, RagnarokException
	{
		Vip vip = new Vip();
		vip.setID(rs.getInt("id"));
		vip.setCharSlotCount(rs.getByte("char_slot_count"));
		vip.setMaxStorage(rs.getShort("max_storage"));

		return vip;
	}

	/**
	 * Carrega todas as informações necessários do grupo de uma determinada conta.
	 * Necessário ter definido o código de identificação do grupo com a conta.
	 * @param account conta do qual deseja carregar as informações do grupo.
	 * @throws RagnarokException se houver falha com a conexão.
	 */

	public void load(Account account) throws RagnarokException
	{
		String table = Tables.getInstance().getAccountsGroup();
		String sql = format("SELECT current_group, old_group, vip, timeout FROM %s WHERE accountid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, account.getID());

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				throw new RagnarokException("grupo de conta '%d' não encontrado", account.getID());

			AccountGroup accountGroup = account.getGroup();
			accountGroup.getTime().set(rs.getDate("timeout").getTime());

			int currentGroupID = rs.getInt("current_group");
			int oldGroupID = rs.getInt("old_group");
			int vipID = rs.getInt("vip");

			if (currentGroupID > 0)
			{
				Group group = groups.get(currentGroupID);

				if (group == null)
					group = getGroup(currentGroupID);

				accountGroup.setCurrentGroup(group);
			}

			if (oldGroupID > 0)
			{
				Group group = groups.get(currentGroupID);

				if (group == null)
					group = getGroup(oldGroupID);

				accountGroup.setOldGroup(group);
			}

			if (vipID > 0)
			{
				Vip vip = vips.get(vipID);

				if (vip == null)
					vip = getVip(vipID);

				accountGroup.setVip(vip);
			}

			logDebug("dados do grupo de uma conta recarregados (aid: %d).\n", account.getID());

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Permite obter das informações de um determinado grupo do banco de dados.
	 * @param int groupID código de identificação do grupo do qual deseja.
	 * @throws RagnarokException falha de conexão ou inconsistência nos dados.
	 */

	public Group getGroup(int groupID) throws RagnarokException
	{
		Group group = groups.get(groupID);

		if (group != null)
			return group;

		String table = Tables.getInstance().getGroups();
		String sql = format("SELECT id, access_level, name, parent, log_commands FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, groupID);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				throw new RagnarokException("grupo de conta '%d' não encontrado", groupID);

			group = newGroup(rs);

			if (groups.add(group.getID(), group))
			{
				int csize = group.getCommands().size();
				int psize = group.getPermissions().size();

				logDebug("novo grupo carregado do banco de dados (gid: %d, commands: %d, permissions: %d).\n", group.getID(), csize, psize);
			}

			return group;

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
		String table = Tables.getInstance().getGroupsPermissions();
		String sql = format("SELECT permission, enabled FROM %s WHERE groupid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, group.getID());

			ResultSet rs = ps.executeQuery();
			GroupPermissions permissions = group.getPermissions();

			while (rs.next())
				permissions.set(rs.getString("permission"), rs.getInt("enabled"));

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
		String table = Tables.getInstance().getGroupsCommands();
		String sql = format("SELECT command, enabled FROM %s WHERE groupid = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, group.getID());

			ResultSet rs = ps.executeQuery();
			GroupCommands commands = group.getCommands();

			while (rs.next())
				commands.set(rs.getString("command"), rs.getInt("enabled"));

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}		
	}

	/**
	 * Permite obter das informações de um determinado grupo do banco de dados.
	 * @param int vipID código de identificação do grupo do qual deseja.
	 * @throws RagnarokException falha de conexão ou inconsistência nos dados.
	 */

	public Vip getVip(int vipID) throws RagnarokException
	{
		Vip vip = vips.get(vipID);

		if (vip != null)
			return vip;

		String table = Tables.getInstance().getVips();
		String sql = format("name, groupid, char_slot_count, max_storage FROM %s WHERE id = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setInt(1, vipID);

			ResultSet rs = ps.executeQuery();

			if (!rs.next())
				throw new RagnarokException("acesso vip '%d' não encontrado", vipID);

			vip = newVip(rs);

			if (vips.add(vipID, vip))
				logDebug("novo vip carregado do banco de dados (vid: %d).\n", vip.getID());

			return vip;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}	
	}

	/**
	 * A inicialização desse grupo consiste em carregar todas as informações de grupos de conta e vip.
	 * @throws RagnarokException apenas se houver falha de conexão com o banco de dados.
	 */

	public void init()
	{
		groups = new IntegerLittleMap<>();
		vips = new IntegerLittleMap<>();

		try {
			initGroups();
			initVips();
		} catch (RagnarokException e) {
			logError("falha ao tentar iniciar o controle de grupos:\n");
			logExeception(e);
		}
	}

	/**
	 * Carrega todas as informações necessárias para a criação de um grupo de contas no sistema.
	 * @throws RagnarokException apenas se houver falha de conexão com o banco de dados.
	 */

	private void initGroups() throws RagnarokException
	{
		String table = Tables.getInstance().getGroups();
		String sql = format("SELECT id FROM %s", table);

		try {

			PreparedStatement ps = prepare(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				Group group = getGroup(rs.getInt("id"));
				groups.add(group.getID(), group);
			}

			logDebug("foram encontrados %d grupos em '%s'.\n", groups.size(), table);

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Carrega todas as informações necessárias para a criação de um vip no sistema.
	 * @throws RagnarokException apenas se houver falha de conexão com o banco de dados.
	 */

	private void initVips() throws RagnarokException
	{
		String table = Tables.getInstance().getVips();
		String sql = format("SELECT id FROM %s", table);

		try {

			PreparedStatement ps = prepare(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				Vip vip = getVip(rs.getInt("id"));
				vips.add(vip.getID(), vip);
			}

			logDebug("foram encontrados %d vip em '%s'.\n", groups.size(), table);

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}

	}

	/**
	 * Libera todos os objetos contidos em cache neste controle (contas de grupo e vip).
	 */

	public void destroy()
	{
		clear();

		groups = null;
		vips = null;
	}

	/**
	 * Realiza uma limpeza no cache que mantém dados de grupos e acessos vip carregados.
	 */

	public void clear()
	{
		logDebug("liberando %d grupo de contas e %d vip liberados.\n", groups.size(), vips.size());

		groups.clear();
		vips.clear();
	}
}

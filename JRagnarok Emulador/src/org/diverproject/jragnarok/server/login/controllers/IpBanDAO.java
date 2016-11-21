package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.DynamicList;
import org.diverproject.util.lang.Bits;
import org.diverproject.util.lang.IntUtil;

/**
 * <h1>DAO para Banimento de Endere�os IP</h1>
 *
 * <p>Essa DAO ir� permitir a comunica��o direta entre o sistema e o banco de dados.
 * Permite realizar inser��o de novas informa��es como selecionar e verificar.
 * Tamb�m permite realizar uma limpeza removendo banimentos que j� expiraram.</p>
 *
 * @see AbstractControl
 * @see IpBanList
 *
 * @author Andrew
 */

public class IpBanDAO extends AbstractControl
{
	/**
	 * Cria um nova DAO para realizar consultas sobre banimento de endere�os IP.
	 * @param connection conex�o com o banco de dados que ser� usada para tal.
	 */

	public IpBanDAO(Connection connection)
	{
		super(connection);
	}

	/**
	 * Cria um novo objeto para armazenar informa��es de um banimento feito.
	 * @param rs conjunto de resultados de uma consulta a ser considerado.
	 * @return aquisi��o do objeto com os valores definidos a partir da consulta.
	 * @throws SQLException apenas se alguma informa��o n�o puder ser obtida.
	 */

	private IpBanList newIpBanList(ResultSet rs) throws SQLException
	{
		IpBanList list = new IpBanList();
		list.setAddressList(rs.getString("address_list"));
		list.getBanTime().set(rs.getLong("ban_time"));
		list.getResumeTime().set(rs.getLong("resume_time"));
		list.setReason(rs.getString("reason"));

		return list;
	}

	/**
	 * Cria uma lista contendo todos os banimentos em que um IP possa estar.
	 * O banimento dos endere�os de IP podem ser feito por escala (127.*.*.*).
	 * @param ip endere�o de IP do qual deseja selecionar os banimentos.
	 * @return lista contendo todos os banimentos existentes.
	 * @throws RagnarokException apenas por falha de conex�o.
	 */

	public List<IpBanList> select(int ip) throws RagnarokException
	{
		String table = Tables.getInstance().getIpBan();
		String sql = format("SELECT address_list, ban_time, resume_time, reason"
						+ " FROM %s WHERE resume_time > NOW()"
						+ " AND (address_list = ? OR address_list = ? OR"
						+ " address_list = ? OR address_list = ?)", table);

		List<IpBanList> lists = new DynamicList<>();

		try {

			int a = IntUtil.parseByte(Bits.byteOf(ip, 4));
			int b = IntUtil.parseByte(Bits.byteOf(ip, 3));
			int c = IntUtil.parseByte(Bits.byteOf(ip, 2));
			int d = IntUtil.parseByte(Bits.byteOf(ip, 1));

			PreparedStatement ps = prepare(sql);
			ps.setString(1, format("%d.*.*.*", a));
			ps.setString(2, format("%d.%d.*.*", a, b));
			ps.setString(3, format("%d.%d.%d.*", a, b, c));
			ps.setString(4, format("%d.%d.%d.%d", a, b, c, d));

			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				IpBanList list = newIpBanList(rs);
				lists.add(list);
			}

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}

		return lists;
	}

	/**
	 * Inserir um novo conjunto de endere�os de IP banidos no banco de dados.
	 * @param list objeto contendo as informa��es referentes ao banimento.
	 * @return true se conseguir inserir ou false caso contr�rio.
	 * @throws RagnarokException apenas por falha de conex�o.
	 */

	public boolean insert(IpBanList list) throws RagnarokException
	{
		String table = Tables.getInstance().getIpBan();
		String sql = format("INSERT INTO %s (address_list, ban_time, resume_time, reason) VALUES (?, ?, ?, ?)", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, list.getAdressList());
			ps.setTimestamp(2, list.getBanTime().toTimestamp());
			ps.setTimestamp(3, list.getResumeTime().toTimestamp());
			ps.setString(4, list.getReason());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {

			if (e.getErrorCode() == DUPLICATED_KEY)
				return update(list);

			throw new RagnarokException(e);
		}
	}

	/**
	 * Realiza a atualiza��o de um determinado banimento de endere�os de IP.
	 * @param list objeto contendo as informa��es do banimento para atualizar.
	 * @return true se conseguir atualizar ou false se n�o encontrar o endere�o.
	 * @throws RagnarokException apenas por falha de conex�o.
	 */

	private boolean update(IpBanList list) throws RagnarokException
	{
		String table = Tables.getInstance().getIpBan();
		String sql = format("UPDATE %s SET ban_time = ?, resume_time = ?, reason = ? WHERE address_list = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setTimestamp(1, list.getBanTime().toTimestamp());
			ps.setTimestamp(2, list.getResumeTime().toTimestamp());
			ps.setString(3, list.getReason());
			ps.setString(4, list.getAdressList());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Remove um determinado conjunto de endere�os de IP banidos no sistema.
	 * @param list objeto contendo as informa��es referentes ao banimento.
	 * @return true se conseguir remover ou false caso contr�rio.
	 * @throws RagnarokException apenas por falha de conex�o.
	 */

	public boolean remove(IpBanList list) throws RagnarokException
	{
		String table = Tables.getInstance().getIpBan();
		String sql = format("DROP FROM %s WHERE address_list = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, list.getAdressList());

			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Realiza uma limpeza nos endere�os de IP banidos removendo todos os expirados.
	 * @return quantidade de banimentos que foram removidos (expirados).
	 * @throws RagnarokException apenas por falha de conex�o.
	 */

	public int cleanup() throws RagnarokException
	{
		String table = Tables.getInstance().getIpBan();
		String sql = format("DROP FROM %s WHERE resume_time <= NOW()", table);

		try {

			PreparedStatement ps = prepare(sql);

			return ps.executeUpdate();

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		return description.toString();
	}
}

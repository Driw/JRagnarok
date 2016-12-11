package org.diverproject.jragnarok.server.login.control;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.now;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logWarning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.AbstractControl;
import org.diverproject.jragnarok.server.Tables;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.DynamicList;
import org.diverproject.util.collection.abstraction.NodeList;
import org.diverproject.util.lang.Bits;
import org.diverproject.util.lang.IntUtil;

/**
 * <h1>Controle para Banimento de Endere�os de IP</h1>
 *
 * <p>Controle para gerenciar os endere�os de IP (ou conjuntos) que estejam banidos do sistema.
 * Caso um endere�o tenha sido banido do sistema, os servidor de acesso n�o permitir� o acesso do mesmo.
 * Para facilitar a busca por endere�os de IP banidos do sistema � utilizado uma lista como cache.</p>
 *
 * <p>Todos os m�todos ir�o acessar o cache como tamb�m ser� aplicado as mudan�as no banco de dados.
 * As opera��es dispon�veis permitem saber se um endere�o de IP se encontra em alguma lista banida.
 * Tal como � permitido selecionar listas de banimentos, adicionar, excluir ou atualizar as inforam��es.</p>
 *
 * @see IpBanList
 * @see NodeList
 * @see Connection
 *
 * @author Andrew
 */

public class IpBanControl extends AbstractControl
{
	/**
	 * Cache em lista que ir� armazenar os endere�os de IP banidos.
	 */
	private final NodeList<IpBanList> cache;

	/**
	 * Cria um novo controle para manter endere�os de IP banidos no sistema.
	 * @param connection conex�o com o banco de dados a ser considerada.
	 */

	public IpBanControl(Connection connection)
	{
		super(connection);

		cache = new NodeList<>();
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
	 * Cria uma lista contendo todos os conjuntos de endere�os de IP que foram banidos do sistema.
	 * Os endere�os de IP podem ser banidos atrav�s de escalas que s�o feitas por: <b>(127.*.*.*)</b>.
	 * @param ip endere�o de IP codificado em um n�mero inteiro do qual deseja listar os conjuntos.
	 * @return lista contendo todos os conjuntos de endere�os de IP banidos que cont�m o IP acima.
	 * @return true se estiver contido em ao menos uma lista ou false caso contr�rio.
	 */

	public List<IpBanList> get(int ip) throws RagnarokException
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
	 * Verifica se um determinado endere�o de IP se encontra em alguma das listas de banimentos.
	 * @param ip endere�o de IP codificado em um n�mero inteiro que ser� verificado.
	 * @return true se estiver contido em ao menos uma lista ou false caso contr�rio.
	 */

	public boolean addressBanned(int ip)
	{
		for (IpBanList list : cache)
			if (list.contains(ip))
				return true;

		try {

			List<IpBanList> lists = get(ip);

			for (IpBanList list : lists)
				cache.add(list);

			return lists.size() > 0;

		} catch (RagnarokException e) {
			logWarning("falha ao verificar endere�o (%s).\n", SocketUtil.socketIP(ip));
			logExeception(e);
		}

		return false;
	}

	/**
	 * Adiciona as informa��es de um determinado conjunto de endere�os IP no sistema.
	 * @param list conjunto de endere�os de IP banidos do qual deseja adicionar.
	 * @return true se conseguir adicionar o banimento ou false se j� existir.
	 * @throws RagnarokException apenas por falha de conex�o com o banco de dados.
	 */

	public boolean add(IpBanList list) throws RagnarokException
	{
		if (cache.contains(list))
			return false;

		cache.add(list);

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
	 * Atualiza as informa��es de um determinado banimento de endere�os IP no sistema.
	 * @param list conjunto de endere�os de IP banidos do qual deseja atualizar.
	 * @return true se conseguir atualizar ou false caso n�o encontre o(s) endere�o(s).
	 * @throws RagnarokException apenas por falha de conex�o com o banco de dados.
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
	 * Remove uma lista de endere�os IP que tenha sido banido no sistema do cache e banco de dados.
	 * @param list conjunto de endere�os de IP banidos do qual deseja remover da lista negra.
	 * @return true se conseguir remover o banimento do cache ou do banco de dados.
	 * @throws RagnarokException apenas por falha de conex�o com o banco de dados.
	 */

	public boolean remove(IpBanList list) throws RagnarokException
	{
		String table = Tables.getInstance().getIpBan();
		String sql = format("DELETE FROM %s WHERE address_list = ?", table);

		try {

			PreparedStatement ps = prepare(sql);
			ps.setString(1, list.getAdressList());

			return cache.remove(list) || ps.executeUpdate() == 1;

		} catch (SQLException e) {
			throw new RagnarokException(e);
		}
	}

	/**
	 * Realiza uma limpeza em todos os banimentos de endere�os de IP j� teve seu tempo expirado.
	 * Inicia a limpeza primeiramente pelo banco de dados e em seguida do cache no sistema.
	 * @return quantidade de conjuntos de endere�os de IP banidos que foram exclu�dos.
	 * @throws RagnarokException apenas por falha de conex�o com o banco de dados.
	 */

	public int cleanup() throws RagnarokException
	{
		String table = Tables.getInstance().getIpBan();
		String sql = format("DELETE FROM %s WHERE resume_time <= NOW()", table);

		try {

			PreparedStatement ps = prepare(sql);
			int clear = ps.executeUpdate();

			for (int i = 0; i < cache.size(); i++)
				if (cache.get(i).getResumeTime().get() <= now())
					cache.remove(i);

			return clear;

		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Limpa todos os endere�os de IP que foram banidos em cache no controle.
	 * Essa limpeza n�o ser� aplicada aos endere�os que est�o no banco de dados.
	 */

	public void clear()
	{
		cache.clear();
	}

	@Override
	public void toString(ObjectDescription description)
	{
		description.append("cacheSize", cache.size());
	}
}

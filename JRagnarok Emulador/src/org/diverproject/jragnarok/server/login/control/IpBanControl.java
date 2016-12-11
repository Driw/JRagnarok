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
 * <h1>Controle para Banimento de Endereços de IP</h1>
 *
 * <p>Controle para gerenciar os endereços de IP (ou conjuntos) que estejam banidos do sistema.
 * Caso um endereço tenha sido banido do sistema, os servidor de acesso não permitirá o acesso do mesmo.
 * Para facilitar a busca por endereços de IP banidos do sistema é utilizado uma lista como cache.</p>
 *
 * <p>Todos os métodos irão acessar o cache como também será aplicado as mudanças no banco de dados.
 * As operações disponíveis permitem saber se um endereço de IP se encontra em alguma lista banida.
 * Tal como é permitido selecionar listas de banimentos, adicionar, excluir ou atualizar as inforamções.</p>
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
	 * Cache em lista que irá armazenar os endereços de IP banidos.
	 */
	private final NodeList<IpBanList> cache;

	/**
	 * Cria um novo controle para manter endereços de IP banidos no sistema.
	 * @param connection conexão com o banco de dados a ser considerada.
	 */

	public IpBanControl(Connection connection)
	{
		super(connection);

		cache = new NodeList<>();
	}

	/**
	 * Cria um novo objeto para armazenar informações de um banimento feito.
	 * @param rs conjunto de resultados de uma consulta a ser considerado.
	 * @return aquisição do objeto com os valores definidos a partir da consulta.
	 * @throws SQLException apenas se alguma informação não puder ser obtida.
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
	 * Cria uma lista contendo todos os conjuntos de endereços de IP que foram banidos do sistema.
	 * Os endereços de IP podem ser banidos através de escalas que são feitas por: <b>(127.*.*.*)</b>.
	 * @param ip endereço de IP codificado em um número inteiro do qual deseja listar os conjuntos.
	 * @return lista contendo todos os conjuntos de endereços de IP banidos que contém o IP acima.
	 * @return true se estiver contido em ao menos uma lista ou false caso contrário.
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
	 * Verifica se um determinado endereço de IP se encontra em alguma das listas de banimentos.
	 * @param ip endereço de IP codificado em um número inteiro que será verificado.
	 * @return true se estiver contido em ao menos uma lista ou false caso contrário.
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
			logWarning("falha ao verificar endereço (%s).\n", SocketUtil.socketIP(ip));
			logExeception(e);
		}

		return false;
	}

	/**
	 * Adiciona as informações de um determinado conjunto de endereços IP no sistema.
	 * @param list conjunto de endereços de IP banidos do qual deseja adicionar.
	 * @return true se conseguir adicionar o banimento ou false se já existir.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
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
	 * Atualiza as informações de um determinado banimento de endereços IP no sistema.
	 * @param list conjunto de endereços de IP banidos do qual deseja atualizar.
	 * @return true se conseguir atualizar ou false caso não encontre o(s) endereço(s).
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
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
	 * Remove uma lista de endereços IP que tenha sido banido no sistema do cache e banco de dados.
	 * @param list conjunto de endereços de IP banidos do qual deseja remover da lista negra.
	 * @return true se conseguir remover o banimento do cache ou do banco de dados.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
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
	 * Realiza uma limpeza em todos os banimentos de endereços de IP já teve seu tempo expirado.
	 * Inicia a limpeza primeiramente pelo banco de dados e em seguida do cache no sistema.
	 * @return quantidade de conjuntos de endereços de IP banidos que foram excluídos.
	 * @throws RagnarokException apenas por falha de conexão com o banco de dados.
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
	 * Limpa todos os endereços de IP que foram banidos em cache no controle.
	 * Essa limpeza não será aplicada aos endereços que estão no banco de dados.
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

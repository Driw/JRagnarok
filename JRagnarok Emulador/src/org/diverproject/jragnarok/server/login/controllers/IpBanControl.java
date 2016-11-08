package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.jragnarok.JRagnarokUtil.now;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logWarning;

import java.sql.Connection;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.NodeList;

/**
 * <h1>Controle para Banimento de Endere�os de IP</h1>
 *
 * <p>Esse controle permite realizar o banimento de endere�os de IP no sistema.
 * Possui uma lista desses banimentos internamente como cache dando prioridade para tal.
 * Todas as opera��es ir�o executar a cache e garantir o mesmo no banco de dados.</p>
 *
 * @see IpBanDAO
 * @see IpBanList
 * @see NodeList
 * @see Connection
 *
 * @author Andrew
 */

public class IpBanControl
{
	/**
	 * DAO para a comunica��o do sistema com o banco de dados.
	 */
	private final IpBanDAO dao;

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
		cache = new NodeList<>();
		dao = new IpBanDAO(connection);
	}

	/**
	 * Verifica se um determinado endere�o de IP se encontra banido.
	 * @param ip endere�o de IP codificado em um n�mero inteiro.
	 * @return true se estiver banido ou false caso contr�rio.
	 */

	public boolean addressBanned(int ip)
	{
		for (IpBanList list : cache)
			if (list.contains(ip))
				return true;

		try {

			List<IpBanList> lists = dao.select(ip);

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
	 * Adiciona um novo banimento de endere�os de IP no sistema ou atualiza se j� existir.
	 * @param list objeto contendo informa��es para o banimento.
	 */

	public void add(IpBanList list)
	{
		try {

			if (dao.insert(list))
				if (!cache.contains(list))
					cache.add(list);

		} catch (RagnarokException e) {
			logWarning("falha ao banir endere�os (%s).\n", list.getAdressList());
			logExeception(e);
		}
	}

	/**
	 * Remove uma determinada lista de endere�os de IP a serem banidos.
	 * Se houver o banimento em cache ser� removido do mesmo tamb�m.
	 * @param list objeto contendo informa��es para o banimento.
	 */

	public void remove(IpBanList list)
	{
		try {

			if (dao.remove(list))
				cache.remove(list);

		} catch (RagnarokException e) {
			logWarning("falha ao banir endere�os (%s).\n", list.getAdressList());
			logExeception(e);
		}
	}

	/**
	 * Realiza uma limpeza em todos os banimentos de endere�os de IP j� expirados.
	 * Inicia o procedimento pelo banco de dados e depois pelo cache do sistema.
	 */

	public void cleanup()
	{
		try {

			logWarning("%s lista de endere�os liberados (%d).\n", dao.cleanup());

			for (int i = 0; i < cache.size(); i++)
				if (cache.get(i).getResumeTime().get() <= now())
					cache.remove(i);

		} catch (RagnarokException e) {
			logWarning("falha ao banir limpar endere�os.\n");
			logExeception(e);
		}
	}

	/**
	 * Remove todos os banimentos armazenados em cache.
	 */

	public void clear()
	{
		cache.clear();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("cacheSize", cache.size());

		return description.toString();
	}
}

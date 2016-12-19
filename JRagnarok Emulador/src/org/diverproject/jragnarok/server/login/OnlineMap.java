package org.diverproject.jragnarok.server.login;

import java.util.Iterator;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Mapeador de Jogadores Online</h1>
 *
 * <p>Através dessa coleção (mapa) um determinado serviço poderá saber se um jogador está online.
 * Armazena em cache algumas informações do jogador online como a conta e servidor de personagem.
 * É possível saber se está online, adicionar como online, remover e limpar o cache.</p>
 *
 * @see OnlineLogin
 * @see IntegerLittleMap
 * @see Iterable
 *
 * @author Andrew
 */

public class OnlineMap implements Iterable<OnlineLogin>
{
	/**
	 * Mapeamento dos jogadores que se encontram online no sistema.
	 */
	private final Map<Integer, OnlineLogin> onlines;

	/**
	 * Cria um novo controle que permite controlar os jogadores online no sistema.
	 * Para isso é necessário definir um mapeador de temporizadores já que os objetos
	 * que mantém jogadores como online utilizam temporizadores para tal.
	 * @param timers mapeamento dos temporizadores do sistema.
	 */

	public OnlineMap()
	{
		this.onlines = new IntegerLittleMap<>();
	}

	/**
	 * Obtém informações de um determinado jogador online pelo seu ID.
	 * @param id código de identificação da conta do jogador desejado.
	 * @return aquisição do objeto com as informações do jogador online.
	 */

	public OnlineLogin get(int id)
	{
		return onlines.get(id);
	}

	/**
	 * Adiciona um novo jogador como online através das informações abaixo:
	 * @param online informações referentes ao jogador online.
	 */

	public void add(OnlineLogin online)
	{
		if (online != null && online.getAccountID() > 0)
			onlines.add(online.getAccountID(), online);
	}

	/**
	 * Remove um jogador online do sistema conforme informações abaixo:
	 * @param accountID código de identificação da conta online.
	 */

	public void remove(int accountID)
	{
		if (accountID > 0)
			onlines.removeKey(accountID);
	}

	/**
	 * Remove todas as informações contidas dos jogadores online no sistema.
	 */

	public void clear()
	{
		onlines.clear();
	}

	/**
	 * Seleciona todos os acessos online que estejam definidos a um determinado servidor de personagem.
	 * @param serverID código de identificação do servidor de personagem que será considerado.
	 * @return aquisição de uma fila com todos os acessos online respectivos ao servidor de personagem.
	 */

	public Queue<OnlineLogin> list(int serverID)
	{
		Queue<OnlineLogin> queue = new DynamicQueue<>();

		for (OnlineLogin online : onlines)
			if (online.getCharServerID() == serverID)
				queue.offer(online);

		return queue;
	}

	@Override
	public Iterator<OnlineLogin> iterator()
	{
		return onlines.iterator();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("size", onlines.size());

		return description.toString();
	}
}

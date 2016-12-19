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
 * <p>Atrav�s dessa cole��o (mapa) um determinado servi�o poder� saber se um jogador est� online.
 * Armazena em cache algumas informa��es do jogador online como a conta e servidor de personagem.
 * � poss�vel saber se est� online, adicionar como online, remover e limpar o cache.</p>
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
	 * Para isso � necess�rio definir um mapeador de temporizadores j� que os objetos
	 * que mant�m jogadores como online utilizam temporizadores para tal.
	 * @param timers mapeamento dos temporizadores do sistema.
	 */

	public OnlineMap()
	{
		this.onlines = new IntegerLittleMap<>();
	}

	/**
	 * Obt�m informa��es de um determinado jogador online pelo seu ID.
	 * @param id c�digo de identifica��o da conta do jogador desejado.
	 * @return aquisi��o do objeto com as informa��es do jogador online.
	 */

	public OnlineLogin get(int id)
	{
		return onlines.get(id);
	}

	/**
	 * Adiciona um novo jogador como online atrav�s das informa��es abaixo:
	 * @param online informa��es referentes ao jogador online.
	 */

	public void add(OnlineLogin online)
	{
		if (online != null && online.getAccountID() > 0)
			onlines.add(online.getAccountID(), online);
	}

	/**
	 * Remove um jogador online do sistema conforme informa��es abaixo:
	 * @param accountID c�digo de identifica��o da conta online.
	 */

	public void remove(int accountID)
	{
		if (accountID > 0)
			onlines.removeKey(accountID);
	}

	/**
	 * Remove todas as informa��es contidas dos jogadores online no sistema.
	 */

	public void clear()
	{
		onlines.clear();
	}

	/**
	 * Seleciona todos os acessos online que estejam definidos a um determinado servidor de personagem.
	 * @param serverID c�digo de identifica��o do servidor de personagem que ser� considerado.
	 * @return aquisi��o de uma fila com todos os acessos online respectivos ao servidor de personagem.
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

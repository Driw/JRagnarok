package org.diverproject.jragnarok.server.login.control;

import static org.diverproject.log.LogSystem.logDebug;

import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.login.entities.OnlineLogin;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Controle de Jogadores Online</h1>
 *
 * <p>Atrav�s desse controlador um determinado servi�o poder� saber se um jogador est� online.
 * Armazena em cache algumas informa��es do jogador online como a conta e servidor de personagem.
 * � poss�vel saber se est� online, adicionar como online, remover e limpar o cache.</p>
 *
 * @see TimerMap
 * @see OnlineLogin
 *
 * @author Andrew
 */

public class OnlineControl
{
	/**
	 * Mapeamento dos temporizadores do sistema.
	 */
	private final TimerMap timers;

	/**
	 * Mapeamento dos jogadores que se encontram online no sistema.
	 */
	private final Map<Integer, OnlineLogin> cache;

	/**
	 * Cria um novo controle que permite controlar os jogadores online no sistema.
	 * Para isso � necess�rio definir um mapeador de temporizadores j� que os objetos
	 * que mant�m jogadores como online utilizam temporizadores para tal.
	 * @param timers mapeamento dos temporizadores do sistema.
	 */

	public OnlineControl(TimerMap timers)
	{
		this.timers = timers;
		this.cache = new IntegerLittleMap<>();
	}

	/**
	 * Obt�m informa��es de um determinado jogador online pelo seu ID.
	 * @param id c�digo de identifica��o da conta do jogador desejado.
	 * @return aquisi��o do objeto com as informa��es do jogador online.
	 */

	public OnlineLogin get(int id)
	{
		return cache.get(id);
	}

	/**
	 * Adiciona um novo jogador como online atrav�s das informa��es abaixo:
	 * @param online informa��es referentes ao jogador online.
	 */

	public void add(OnlineLogin online)
	{
		if (online.getWaitingDisconnect() != null)
		{
			timers.delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		cache.add(online.getAccountID(), online);

		logDebug("account#%d est� online.\n", online.getAccountID());
	}

	/**
	 * Remove um jogador online do sistema conforme informa��es abaixo:
	 * @param online informa��es referentes ao jogador online.
	 */

	public void remove(OnlineLogin online)
	{
		if (online.getWaitingDisconnect() != null)
		{
			timers.delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		cache.remove(online);

		logDebug("account#%d n�o est� mais online.\n", online.getAccountID());
	}

	/**
	 * Remove um jogador online do sistema conforme informa��es abaixo:
	 * @param accountID c�digo de identifica��o da conta online.
	 */

	public void remove(int accountID)
	{
		remove(cache.get(accountID));
	}

	/**
	 * Remove todas as informa��es contidas dos jogadores online no sistema.
	 */

	public void clear()
	{
		for (OnlineLogin online : cache)
			remove(online);

		cache.clear();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("online", cache.size());

		return description.toString();
	}
}

package org.diverproject.jragnarok.server.login.control;

import static org.diverproject.log.LogSystem.logDebug;

import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.login.structures.OnlineLogin;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Controle de Jogadores Online</h1>
 *
 * <p>Através desse controlador um determinado serviço poderá saber se um jogador está online.
 * Armazena em cache algumas informações do jogador online como a conta e servidor de personagem.
 * É possível saber se está online, adicionar como online, remover e limpar o cache.</p>
 *
 * @see TimerMap
 * @see OnlineLogin
 *
 * @author Andrew
 */

public class OnlineMap
{
	/**
	 * Mapeamento dos jogadores que se encontram online no sistema.
	 */
	private final Map<Integer, OnlineLogin> cache;

	/**
	 * Cria um novo controle que permite controlar os jogadores online no sistema.
	 * Para isso é necessário definir um mapeador de temporizadores já que os objetos
	 * que mantém jogadores como online utilizam temporizadores para tal.
	 * @param timers mapeamento dos temporizadores do sistema.
	 */

	public OnlineMap()
	{
		this.cache = new IntegerLittleMap<>();
	}

	/**
	 * Obtém informações de um determinado jogador online pelo seu ID.
	 * @param id código de identificação da conta do jogador desejado.
	 * @return aquisição do objeto com as informações do jogador online.
	 */

	public OnlineLogin get(int id)
	{
		return cache.get(id);
	}

	/**
	 * Adiciona um novo jogador como online através das informações abaixo:
	 * @param online informações referentes ao jogador online.
	 * @param timers mapeamento dos temporizadores do objeto acima.
	 */

	public void add(OnlineLogin online, TimerMap timers)
	{
		if (online.getWaitingDisconnect() != null)
		{
			timers.delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		cache.add(online.getAccountID(), online);

		logDebug("account#%d está online.\n", online.getAccountID());
	}

	/**
	 * Remove um jogador online do sistema conforme informações abaixo:
	 * @param online informações referentes ao jogador online.
	 * @param timers mapeamento dos temporizadores do objeto acima.
	 */

	public void remove(OnlineLogin online, TimerMap timers)
	{
		if (online.getWaitingDisconnect() != null)
		{
			timers.delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		cache.remove(online);

		logDebug("account#%d não está mais online.\n", online.getAccountID());
	}

	/**
	 * Remove um jogador online do sistema conforme informações abaixo:
	 * @param accountID código de identificação da conta online.
	 * @param timers mapeamento dos temporizadores do objeto acima.
	 */

	public void remove(int accountID, TimerMap timers)
	{
		remove(cache.get(accountID), timers);
	}

	/**
	 * Remove todas as informações contidas dos jogadores online no sistema.
	 * @param timers mapeamento dos temporizadores do objeto acima.
	 */

	public void clear(TimerMap timers)
	{
		for (OnlineLogin online : cache)
			remove(online, timers);

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

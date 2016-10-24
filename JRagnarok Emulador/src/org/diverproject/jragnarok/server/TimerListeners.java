package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

/**
 * <h1>Listeners de Temporizadores</h1>
 *
 * <p>Cole��o que dever� mapear os listeners do sistema em um cole��o por chave.
 * A chave dessa cole��o ser� o pr�prio nome do listener que o vincula.</p>
 *
 * @see Map
 *
 * @author Andrew Mello
 */

public class TimerListeners
{
	/**
	 * Mapa contendo os listeners dos temporizados.
	 */
	private Map<String, TimerListener> listeners;

	/**
	 * Cria uma nova cole��o para mapear temporizadores atrav�s de nomes.
	 */

	public TimerListeners()
	{
		listeners = new StringSimpleMap<>();
	}

	/**
	 * Adiciona um novo listener a lista de listeners dos temporizadores.
	 * @param listener refer�ncia do listener que deseja adicionar.
	 * @param name nome que ser� vinculado a essa listener.
	 */

	public void add(TimerListener listener)
	{
		if (listener == null)
			return;

		if (listeners.containsKey(listener.getName()))
			logWarning("fun��o duplicada (name: %s)", listener.getName());
		else
			listeners.add(listener.getName(), listener);
	}

	/**
	 * Permite remover um determinado temporizador do sistema de temporizadores.
	 * Ser� removido ainda o listener vinculado a essa temporizador se houver.
	 * @param listener refer�ncia do listener que ser� removido do mapa.
	 */

	public void delete(TimerListener listener)
	{
		if (listener != null)
			listeners.removeKey(listener.getName());
	}

}

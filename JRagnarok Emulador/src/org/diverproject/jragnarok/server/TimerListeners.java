package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

/**
 * <h1>Listeners de Temporizadores</h1>
 *
 * <p>Coleção que deverá mapear os listeners do sistema em um coleção por chave.
 * A chave dessa coleção será o próprio nome do listener que o vincula.</p>
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
	 * Cria uma nova coleção para mapear temporizadores através de nomes.
	 */

	public TimerListeners()
	{
		listeners = new StringSimpleMap<>();
	}

	/**
	 * Adiciona um novo listener a lista de listeners dos temporizadores.
	 * @param listener referência do listener que deseja adicionar.
	 * @param name nome que será vinculado a essa listener.
	 */

	public void add(TimerListener listener)
	{
		if (listener == null)
			return;

		if (listeners.containsKey(listener.getName()))
			logWarning("função duplicada (name: %s)", listener.getName());
		else
			listeners.add(listener.getName(), listener);
	}

	/**
	 * Permite remover um determinado temporizador do sistema de temporizadores.
	 * Será removido ainda o listener vinculado a essa temporizador se houver.
	 * @param listener referência do listener que será removido do mapa.
	 */

	public void delete(TimerListener listener)
	{
		if (listener != null)
			listeners.removeKey(listener.getName());
	}

}

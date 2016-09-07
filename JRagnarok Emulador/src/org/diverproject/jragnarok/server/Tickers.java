package org.diverproject.jragnarok.server;

import java.util.Iterator;

import org.diverproject.util.collection.abstraction.DynamicIndex;

/**
 * <h1>Tickers</h1>
 *
 * <p>Indexador para os temporizadores através do tick definido nos mesmos.
 * Possui três métodos simples para manusear o indexador e outro para iterar.
 * Os métodos permitem adicionar, atualizar ou remover um temporizador.</p>
 *
 * @see DynamicIndex
 * @see Iterable
 * @see Timer
 *
 * @author Andrew
 */

public class Tickers implements Iterable<Timer>
{
	/**
	 * Coleção por indexação dinâmica.
	 */
	private DynamicIndex<Timer> timers;

	/**
	 * Inicializa a coleção de indexação dinâmica e permite duas indexações iguais.
	 */

	public Tickers()
	{
		timers = new DynamicIndex<>();
		timers.setRepet(true);
	}

	/**
	 * Adiciona um novo temporizador a indexação de temporizadores.
	 * Caso já tenha sido adicionado esse método será ignorado.
	 * @param timer referência do temporizador que será adicionado.
	 */

	public void add(Timer timer)
	{
		if (!timers.contains(timer))
			timers.add(timer.getTick(), timer);
	}

	/**
	 * Atualiza um determinado temporizador para a sua respectiva posição.
	 * Primeiramente remove o temporizador e o reposiciona conforme seu tick.
	 * @param timer referência do temporizador do qual deseja atualizar.
	 */

	public void update(Timer timer)
	{
		timers.remove(timer);
		timers.add(timer.getTick(), timer);
	}

	/**
	 * Remove um temporizador da indexação de temporizadores.
	 * @param timer referência do temporizador a ser removido.
	 */

	public void remove(Timer timer)
	{
		timers.remove(timer);
	}

	/**
	 * Remove todos os temporizadores existentes na indexação.
	 */

	public void clear()
	{
		timers.clear();
	}

	@Override
	public Iterator<Timer> iterator()
	{
		return timers.iterator();
	}
}

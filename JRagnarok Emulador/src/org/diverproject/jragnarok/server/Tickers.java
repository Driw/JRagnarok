package org.diverproject.jragnarok.server;

import java.util.Iterator;

import org.diverproject.util.collection.abstraction.DynamicIndex;

/**
 * <h1>Tickers</h1>
 *
 * <p>Indexador para os temporizadores atrav�s do tick definido nos mesmos.
 * Possui tr�s m�todos simples para manusear o indexador e outro para iterar.
 * Os m�todos permitem adicionar, atualizar ou remover um temporizador.</p>
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
	 * Cole��o por indexa��o din�mica.
	 */
	private DynamicIndex<Timer> timers;

	/**
	 * Inicializa a cole��o de indexa��o din�mica e permite duas indexa��es iguais.
	 */

	public Tickers()
	{
		timers = new DynamicIndex<>();
		timers.setRepet(true);
	}

	/**
	 * Adiciona um novo temporizador a indexa��o de temporizadores.
	 * Caso j� tenha sido adicionado esse m�todo ser� ignorado.
	 * @param timer refer�ncia do temporizador que ser� adicionado.
	 */

	public void add(Timer timer)
	{
		if (!timers.contains(timer))
			timers.add(timer.getTick(), timer);
	}

	/**
	 * Atualiza um determinado temporizador para a sua respectiva posi��o.
	 * Primeiramente remove o temporizador e o reposiciona conforme seu tick.
	 * @param timer refer�ncia do temporizador do qual deseja atualizar.
	 */

	public void update(Timer timer)
	{
		timers.remove(timer);
		timers.add(timer.getTick(), timer);
	}

	/**
	 * Remove um temporizador da indexa��o de temporizadores.
	 * @param timer refer�ncia do temporizador a ser removido.
	 */

	public void remove(Timer timer)
	{
		timers.remove(timer);
	}

	/**
	 * Remove todos os temporizadores existentes na indexa��o.
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

package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logError;

import java.util.Iterator;

import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.DynamicIndex;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Mapeador de Temporizadores</h1>
 *
 * <p>Classe usada para mapear os temporizadores atrav�s de seus c�digos de identifica��o.
 * Assim permite que temporizadores possuam identifica��es �nicas no sistema.
 * Lembrando que o c�digo de identifica��es deles s�o criados nos pr�prios temporizadores.</p>
 *
 * <p>Al�m disso tamb�m indexa os temporizadores conforme seu pr�ximo hor�rio de execu��o.
 * Ou seja, quanto mais perto for do seu pr�ximo hor�rio mais a frente estar� indexado,
 * podendo assim ser executado primeiro do que outros temporizadores mais tardios.</p>
 *
 * @see Tickers
 * @see Iterable
 * @see IntegerLittleMap
 * @see DynamicIndex
 * @see Map
 *
 * @author Andrew Mello
 */

public class TimerMap implements Iterable<Timer>
{
	/**
	 * Mapa contendo os temporizadores que foram adicionados.
	 */
	private Map<Integer, Timer> timers;

	/**
	 * Lista para indexar os temporizadores em ordem de execu��o.
	 */
	private DynamicIndex<Timer> indexes;

	/**
	 * Inicializa o mapeamento dos temporizadores e indexa��o dos temporizadores e
	 * a cole��o de indexa��o din�mica e permite duas indexa��es iguais.
	 */

	public TimerMap()
	{
		timers = new IntegerLittleMap<>();
		indexes = new DynamicIndex<>();
		indexes.setRepet(true);
	}

	/**
	 * Procedimento que permite adicionar um novo temporizador para ser mapeado.
	 * @param timer refer�ncia do temporizador que dever� ser mapeado.
	 */

	public void add(Timer timer)
	{
		if (timer != null)
		{
			timers.add(timer.getID(), timer);
			indexes.add(timer.getTick(), timer);
		}
	}

	/**
	 * Atualiza um temporizador para que ser� exclu�do assim que expirado.
	 * @param timer refer�ncia do temporizador que deseja alterar.
	 */

	public void setTimerExpired(Timer timer)
	{
		add(timer);

		timer.setInterval(1000);
		timer.getType().set(Timer.TIMER_ONCE_AUTODEL);

		update(timer);
	}

	/**
	 * Atualiza um determinado temporizador para a sua respectiva posi��o.
	 * Primeiramente remove o temporizador e o reposiciona conforme seu tick.
	 * @param timer refer�ncia do temporizador do qual deseja atualizar.
	 */

	public void update(Timer timer)
	{
		indexes.remove(timer); // Remover por objeto j� que pode ter tick duplicado
		indexes.add(timer.getTick(), timer);
	}

	/**
	 * Atualiza um temporizador para auto reiniciar quando for expirado.
	 * Assim sendo dever� ser removido manualmente do sistema.
	 * @param timer refer�ncia do temporizador que ser� atualizado.
	 * @param interval intervalo para que o temporizador seja renovado.
	 */

	public void addInterval(Timer timer, int interval)
	{
		if (interval >= 0)
		{
			add(timer);

			timer.setInterval(interval);
			timer.getType().set(Timer.TIMER_INTERVAL);

			update(timer);
		}

		else
			logError("intervalo inv�lido (timer: %d, listener: %s, data: %d).\n", timer.getID(), timer.getListener().getName(), timer.getData());
	}

	/**
	 * Permite remover um determinado temporizador do sistema de temporizadores.
	 * Ser� removido ainda o listener vinculado a essa temporizador se houver.
	 * @param timer refer�ncia do temporizador que ser� removido do sistema.
	 */

	public void delete(Timer timer)
	{
		if (timer.getListener() != null)
		{
			TimerListeners listeners = TimerSystem.getInstance().getListeners();
			listeners.delete(timer.getListener());
		}

		timer.setListener(null);
		timer.getType().set(Timer.TIMER_ONCE_AUTODEL);
	}

	/**
	 * Executa todos os temporizadores que tiverem o seu tempo expirado no sistema.
	 * @param tick momento no tempo do sistema para atualizar os temporizadores.
	 * @return tempo em milissegundos para o pr�ximo temporizador vencer.
	 */

	public int update(int tick)
	{
		int diff = TimerSystem.MAX_TIMER_INTERVAL;

		for (Timer timer : this)
		{
			diff = timer.getTick() - tick;

			if (diff > 0)
				break;

			timer.getType().set(Timer.TIMER_REMOVE);

			if (timer.getListener() != null)
			{
				if (diff < -TimerSystem.MAX_TIMER_INTERVAL)
					timer.getListener().onCall(timer, tick);
				else
					timer.getListener().onCall(timer, timer.getTick());
			}

			if (timer.getType().is(Timer.TIMER_REMOVE))
			{
				timer.getType().unset(Timer.TIMER_REMOVE);

				switch (timer.getType().getValue())
				{
					default:
					case Timer.TIMER_ONCE_AUTODEL:
						timer.getType().setValue(0);
						timers.remove(timer);
						indexes.remove(timer);
						break;

					case Timer.TIMER_INTERVAL:
						if (timer.getTick() - tick < -1000)
							timer.setTick(tick + timer.getInterval());
						else
							timer.setTick(timer.getTick() + timer.getInterval());
						break;
				}
			}
		}

		return diff;
	}

	/**
	 * Limpa o mapeador e indexador de temporizadores, removendo todos eles de suas cole��es.
	 * Caso os temporizadores n�o esteja armazenados em outros lugares ser�o liberados em mem�ria.
	 */

	public void clear()
	{
		timers.clear();
		indexes.clear();
	}

	@Override
	public Iterator<Timer> iterator()
	{
		synchronized (indexes)
		{
			Iterator<Timer> iterator = indexes.iterator();

			return iterator;
		}
	}
}

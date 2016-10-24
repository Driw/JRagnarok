package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logError;

import java.util.Iterator;

import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.DynamicIndex;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Mapeador de Temporizadores</h1>
 *
 * <p>Classe usada para mapear os temporizadores através de seus códigos de identificação.
 * Assim permite que temporizadores possuam identificações únicas no sistema.
 * Lembrando que o código de identificações deles são criados nos próprios temporizadores.</p>
 *
 * <p>Além disso também indexa os temporizadores conforme seu próximo horário de execução.
 * Ou seja, quanto mais perto for do seu próximo horário mais a frente estará indexado,
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
	 * Lista para indexar os temporizadores em ordem de execução.
	 */
	private DynamicIndex<Timer> indexes;

	/**
	 * Inicializa o mapeamento dos temporizadores e indexação dos temporizadores e
	 * a coleção de indexação dinâmica e permite duas indexações iguais.
	 */

	public TimerMap()
	{
		timers = new IntegerLittleMap<>();
		indexes = new DynamicIndex<>();
		indexes.setRepet(true);
	}

	/**
	 * Procedimento que permite adicionar um novo temporizador para ser mapeado.
	 * @param timer referência do temporizador que deverá ser mapeado.
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
	 * Atualiza um temporizador para que será excluído assim que expirado.
	 * @param timer referência do temporizador que deseja alterar.
	 */

	public void setTimerExpired(Timer timer)
	{
		add(timer);

		timer.setInterval(1000);
		timer.getType().set(Timer.TIMER_ONCE_AUTODEL);

		update(timer);
	}

	/**
	 * Atualiza um determinado temporizador para a sua respectiva posição.
	 * Primeiramente remove o temporizador e o reposiciona conforme seu tick.
	 * @param timer referência do temporizador do qual deseja atualizar.
	 */

	public void update(Timer timer)
	{
		indexes.remove(timer); // Remover por objeto já que pode ter tick duplicado
		indexes.add(timer.getTick(), timer);
	}

	/**
	 * Atualiza um temporizador para auto reiniciar quando for expirado.
	 * Assim sendo deverá ser removido manualmente do sistema.
	 * @param timer referência do temporizador que será atualizado.
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
			logError("intervalo inválido (timer: %d, listener: %s, data: %d).\n", timer.getID(), timer.getListener().getName(), timer.getData());
	}

	/**
	 * Permite remover um determinado temporizador do sistema de temporizadores.
	 * Será removido ainda o listener vinculado a essa temporizador se houver.
	 * @param timer referência do temporizador que será removido do sistema.
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
	 * @return tempo em milissegundos para o próximo temporizador vencer.
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
	 * Limpa o mapeador e indexador de temporizadores, removendo todos eles de suas coleções.
	 * Caso os temporizadores não esteja armazenados em outros lugares serão liberados em memória.
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

package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.server.TimerType.TIMER_INTERVAL;
import static org.diverproject.jragnarok.server.TimerType.TIMER_LOOP;
import static org.diverproject.jragnarok.server.TimerType.TIMER_ONCE_AUTODEL;
import static org.diverproject.jragnarok.server.TimerType.TIMER_REMOVE;
import static org.diverproject.log.LogSystem.logError;

import java.util.Iterator;

import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.DynamicIndex;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;
import org.diverproject.util.collection.abstraction.NodeList;

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
	 * Lista contendo n�s dos temporizadores ordenados.
	 */
	private NodeList<Timer> indexes;

	/**
	 * Inicializa o mapeamento dos temporizadores e indexa��o dos temporizadores e
	 * a cole��o de indexa��o din�mica e permite duas indexa��es iguais.
	 */

	public TimerMap()
	{
		timers = new IntegerLittleMap<>();
		indexes = new NodeList<>();
	}

	/**
	 * Cria um novo temporizador e o adiciona ao mapa de temporizadores.
	 * O c�digo de identifica��o dele � auto incremental no sistema.
	 * @return aquisi��o do objeto temporizador para ser utilizado.
	 */

	public Timer acquireTimer()
	{
		Timer timer = new Timer();
		timers.add(timer.getID(), timer);

		return timer;
	}

	/**
	 * Adiciona um novo temporizador de forma que seja executado uma s� vez.
	 * @param timer refer�ncia do temporizador que dever� ser mapeado.
	 */

	public void add(Timer timer)
	{
		if (timer != null)
		{
			if (!timers.containsKey(timer.getID()))
				timers.add(timer.getID(), timer);

			timer.setInterval(0);
			timer.setType(TIMER_ONCE_AUTODEL);

			update(timer);
		}
	}

	/**
	 * Adiciona o temporizador para ser executado ap�s o intervalo definido.
	 * @param timer refer�ncia do temporizador que ser� atualizado.
	 * @param interval intervalo de espera para a execu��o do loop.
	 */

	public void addInterval(Timer timer, int interval)
	{
		if (interval >= 0)
		{
			if (!timers.containsKey(timer.getID()))
				timers.add(timer.getID(), timer);

			timer.setType(TIMER_INTERVAL);
			timer.setInterval(interval);

			update(timer);
		}

		else
			logError("intervalo inv�lido (timer: %d, listener: %s).\n", timer.getID(), timer.getListener().getName());
	}

	/**
	 * Adiciona o temporizador para ser executado em loops conforme o intervalo definido.
	 * @param timer refer�ncia do temporizador que ser� atualizado.
	 * @param interval intervalo para que o temporizador seja renovado.
	 */

	public void addLoop(Timer timer, int interval)
	{
		if (interval >= 0)
		{
			if (!timers.containsKey(timer.getID()))
				timers.add(timer.getID(), timer);

			timer.setType(TIMER_LOOP);
			timer.setInterval(interval);

			update(timer);
		}

		else
			logError("intervalo inv�lido (timer: %d, listener: %s).\n", timer.getID(), timer.getListener().getName());
	}

	/**
	 * Atualiza um determinado temporizador para a sua respectiva posi��o.
	 * Primeiramente remove o temporizador e o reposiciona conforme seu tick.
	 * @param timer refer�ncia do temporizador do qual deseja atualizar.
	 */

	public void update(Timer timer)
	{
		indexes.remove(timer); // Remover por objeto j� que pode ter tick duplicado
		indexes.add(timer);
	}

	/**
	 * Permite remover um determinado temporizador do sistema de temporizadores.
	 * Ser� removido ainda o listener vinculado a essa temporizador se houver.
	 * @param timer refer�ncia do temporizador que ser� removido do sistema.
	 */

	public void delete(Timer timer)
	{
		timer.setListener(null);
		timer.setType(TimerType.TIMER_INVALID);
	}

	/**
	 * Executa todos os temporizadores que tiverem o seu tempo expirado no sistema.
	 * @param now tempo atual que ocorre a atualiza��o em milissegundos.
	 * @param tick milissegundos passados desde a �ltima atualiza��o.
	 */

	public void update(int now, int tick)
	{
		for (Timer timer : this)
		{
			switch (timer.getType())
			{
				case TIMER_ONCE_AUTODEL:
					timer.getListener().onCall(timer, now, tick);
					timer.setType(TIMER_REMOVE);
					break;

				case TIMER_INTERVAL:
					if (now >= timer.getTick() + timer.getInterval())
					{
						timer.getListener().onCall(timer, now, tick);
						timer.setType(TIMER_REMOVE);
					}
					break;

				case TIMER_LOOP:
					if (now >= timer.getTick())
					{
						timer.getListener().onCall(timer, now, tick);
						timer.setTick(now + timer.getInterval());
						update(timer);
					}
					break;

				case TIMER_REMOVE:
					delete(timer);
					break;

				default:
					timers.removeKey(timer.getID());
					indexes.remove(timer);
					break;
			}
		}
	}

	/**
	 * Cria um StringBuffer internamente e vincula o nome de todos os temporizadores.
	 * O nome de cada temporizador � obtido conforme o nome do listener do mesmo.
	 * @return aquisi��o de uma string contendo o nome dos temporizadores.
	 */

	public String getTimerNames()
	{
		StringBuffer buffer = new StringBuffer();

		for (Timer timer : this)
			if (timer.getListener() != null)
				buffer.append(timer.getListener().getName()+ ";");

		return buffer.toString();
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

	/**
	 * @return aquisi��o da quantidade de temporizadores mapeados.
	 */

	public int size()
	{
		return timers.size();
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

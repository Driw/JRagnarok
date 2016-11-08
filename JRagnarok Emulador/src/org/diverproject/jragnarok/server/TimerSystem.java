package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.free;
import static org.diverproject.jragnarok.JRagnarokUtil.i;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;
import org.diverproject.util.TimerTick;
import org.diverproject.util.collection.Map;

/**
 * <h1>Sistema de Temporizadores</h1>
 *
 * <p>Sistema que permite a sincronização e temporização de objetos no emulador.
 * O sistema funciona através de ticks obtidos de TimerTick conforme especificado.
 * A especificação define que cada tick será considerado por milissegundos passados.</p>
 *
 * @see Map
 * @see TimerTick
 *
 * @author Andrew
 */

public class TimerSystem
{
	/**
	 * Quando o sistema foi inicializado.
	 */
	private long started;

	/**
	 * Quando ocorreu o último tick no sistema.
	 */
	private int currentTime;

	/**
	 * Referência do objeto que faz a cronometragem do sistema.
	 */
	private TimerTick ticker;

	/**
	 * Mapeador de temporizadores.
	 */
	private TimerMap timers;

	/**
	 * Cria um novo Sistema de Temporizadores definindo o mapa de temporizadores e listeners.
	 * Também inicializa o cronômetro com TimerTick definindo o intervalo de 1 milissegundo.
	 */

	public TimerSystem()
	{
		ticker = new TimerTick(1);
		timers = new TimerMap();
	}

	/**
	 * TimerMap é uma coleção que armazena os temporizadores existentes no sistema.
	 * Os temporizadores nele são organizados para identificá-los e executá-los.
	 * @return aquisição da coleção que armazena os temporizadores do sistema.
	 */

	public TimerMap getTimers()
	{
		return timers;
	}

	/**
	 * O último tick representa a diferença de tempo entre o último e penúltimo tick.
	 * @return aquisição da quantidade de milissegundos calculados no último tick.
	 */

	public int getCurrentTime()
	{
		return currentTime;
	}

	/**
	 * @return aquisição do tempo em milissegundos que o sistema está ligado.
	 */

	public long getUptime()
	{
		return System.currentTimeMillis() - started;
	}

	/**
	 * A realização do tick permite obter a quantidade de milissegundos passados.
	 * Essa contagem é feita a partir da última chamada desse mesmo método.
	 * Portanto se a primeira chamada é feita em 100 e a segunda em 110 o tick é 10.
	 * @return aquisição de quantos milissegundos se passaram desde o último tick.
	 */

	public int tick()
	{
		int tick = i(ticker.getTicks());
		currentTime = i(ticker.getTicksCount());

		return tick;
	}

	/**
	 * Inicializa o sistema definindo o seu momento de inicialização.
	 * Também defini os ticks para serem contados em milissegundos.
	 */

	public void init()
	{
		if (started > 0)
			return;

		started = System.currentTimeMillis();
		ticker = new TimerTick(1, Long.MAX_VALUE);
	}

	/**
	 * Destrói o sistema para gerenciamento dos temporizadores de um servidor.
	 * Limpa a lista de identificação e indexação dos temporizadores.
	 * Remove o ticker do sistema e reinicia o tempo do sistema.
	 */

	public void destroy()
	{
		timers.clear();
		ticker = null;
		started = 0;
		currentTime = 0;

		free();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("started", new Time(started));
		description.append("currentTime", currentTime);
		description.append("tick", ticker);
		description.append("timers", timers.size());

		return description.toString();
	}
}

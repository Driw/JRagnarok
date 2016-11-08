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
 * <p>Sistema que permite a sincroniza��o e temporiza��o de objetos no emulador.
 * O sistema funciona atrav�s de ticks obtidos de TimerTick conforme especificado.
 * A especifica��o define que cada tick ser� considerado por milissegundos passados.</p>
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
	 * Quando ocorreu o �ltimo tick no sistema.
	 */
	private int currentTime;

	/**
	 * Refer�ncia do objeto que faz a cronometragem do sistema.
	 */
	private TimerTick ticker;

	/**
	 * Mapeador de temporizadores.
	 */
	private TimerMap timers;

	/**
	 * Cria um novo Sistema de Temporizadores definindo o mapa de temporizadores e listeners.
	 * Tamb�m inicializa o cron�metro com TimerTick definindo o intervalo de 1 milissegundo.
	 */

	public TimerSystem()
	{
		ticker = new TimerTick(1);
		timers = new TimerMap();
	}

	/**
	 * TimerMap � uma cole��o que armazena os temporizadores existentes no sistema.
	 * Os temporizadores nele s�o organizados para identific�-los e execut�-los.
	 * @return aquisi��o da cole��o que armazena os temporizadores do sistema.
	 */

	public TimerMap getTimers()
	{
		return timers;
	}

	/**
	 * O �ltimo tick representa a diferen�a de tempo entre o �ltimo e pen�ltimo tick.
	 * @return aquisi��o da quantidade de milissegundos calculados no �ltimo tick.
	 */

	public int getCurrentTime()
	{
		return currentTime;
	}

	/**
	 * @return aquisi��o do tempo em milissegundos que o sistema est� ligado.
	 */

	public long getUptime()
	{
		return System.currentTimeMillis() - started;
	}

	/**
	 * A realiza��o do tick permite obter a quantidade de milissegundos passados.
	 * Essa contagem � feita a partir da �ltima chamada desse mesmo m�todo.
	 * Portanto se a primeira chamada � feita em 100 e a segunda em 110 o tick � 10.
	 * @return aquisi��o de quantos milissegundos se passaram desde o �ltimo tick.
	 */

	public int tick()
	{
		int tick = i(ticker.getTicks());
		currentTime = i(ticker.getTicksCount());

		return tick;
	}

	/**
	 * Inicializa o sistema definindo o seu momento de inicializa��o.
	 * Tamb�m defini os ticks para serem contados em milissegundos.
	 */

	public void init()
	{
		if (started > 0)
			return;

		started = System.currentTimeMillis();
		ticker = new TimerTick(1, Long.MAX_VALUE);
	}

	/**
	 * Destr�i o sistema para gerenciamento dos temporizadores de um servidor.
	 * Limpa a lista de identifica��o e indexa��o dos temporizadores.
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

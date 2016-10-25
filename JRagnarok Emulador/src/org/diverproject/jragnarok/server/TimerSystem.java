package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.free;

import org.diverproject.util.TimerTick;
import org.diverproject.util.collection.Map;
import org.diverproject.util.lang.IntUtil;

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
	 * Tempo m�nimo de um temporizador.
	 */
	public static final int MIN_TIMER_INTERVAL = 20;

	/**
	 * Tempo limite de um temporizador.
	 */
	public static final int MAX_TIMER_INTERVAL = 1000;


	/**
	 * �nica inst�ncia dispon�vel do Sistema de Temporizadores.
	 */
	private static final TimerSystem INSTANCE = new TimerSystem();


	/**
	 * Quando o sistema foi inicializado.
	 */
	private long started;

	/**
	 * Quando ocorreu o �ltimo tick no sistema.
	 */
	private int lastTick;

	/**
	 * Quantos ticks (milissegundos) j� se passaram desde a inicializa��o.
	 */
	private int lastTickCount;

	/**
	 * Refer�ncia do objeto que faz a cronometragem do sistema.
	 */
	private TimerTick tick;

	/**
	 * Mapeador de temporizadores.
	 */
	private TimerMap timers;

	/**
	 * Cria um novo Sistema de Temporizadores definindo o mapa de temporizadores e listeners.
	 * Tamb�m inicializa o cron�metro com TimerTick definindo o intervalo de 1 milissegundo.
	 */

	private TimerSystem()
	{
		tick = new TimerTick(1);
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
	 * Cria um novo temporizador e o adiciona ao mapa de temporizadores.
	 * O c�digo de identifica��o dele � auto incremental no sistema.
	 * @return aquisi��o do objeto temporizador para ser utilizado.
	 */

	public Timer acquireTimer()
	{
		Timer timer = new Timer();
		timers.add(timer);

		return timer;
	}

	/**
	 * Aumenta o tempo de um temporizador em milissegundos especificados.
	 * @param timer refer�ncia do temporizador que ser� atualizado.
	 * @param milisseconds quantidade de tempo em milissegundos.
	 */

	public void addTickTimer(Timer timer, int milisseconds)
	{
		setTickTimer(timer, timer.getTick() + milisseconds);
	}

	/**
	 * Define o tempo de um temporizador em milissegundos especificados.
	 * @param timer refer�ncia do temporizador que ser� atualizado.
	 * @param tick tempo do servidor em milissegundos ligado.
	 */

	private void setTickTimer(Timer timer, int tick)
	{
		if (tick < 0)
			tick = 0;

		timer.setTick(tick);
	}

	/**
	 * A realiza��o do tick permite obter a quantidade de milissegundos passados.
	 * Essa contagem � feita a partir da �ltima chamada desse mesmo m�todo.
	 * Portanto se a primeira chamada � feita em 100 e a segunda em 110 o tick � 10.
	 * @return aquisi��o de quantos milissegundos se passaram desde o �ltimo tick.
	 */

	public int tick()
	{
		lastTick = (int) tick.getTicks();
		lastTickCount = (int) tick.getTicksCount();

		return lastTickCount;
	}

	/**
	 * O �ltimo tick representa a diferen�a de tempo entre o �ltimo e pen�ltimo tick.
	 * @return aquisi��o da quantidade de milissegundos calculados no �ltimo tick.
	 */

	public int getLastTick()
	{
		return lastTick;
	}

	/**
	 * A contagem de ticks � salva no sistema para que possa ser usado se necess�rio.
	 * @return aquisi��o do tempo em milissegundos j� calculados em ticks.
	 */

	public int getLastTickCount()
	{
		return lastTickCount;
	}

	/**
	 * Atualiza o sistema de temporizadores afim de execut�-los se necess�rio.
	 * @param tick momento no tempo do sistema para atualizar os temporizadores.
	 * @return tempo em milissegundos para o pr�ximo temporizador vencer,
	 * ir� considerar os valores limites de MIN_TIMER_INTERVAL e MAX_TIMER_INTERVAL.
	 */

	public int update(int tick)
	{
		int diff = timers.update(tick);

		return IntUtil.limit(diff, MIN_TIMER_INTERVAL, MAX_TIMER_INTERVAL);
	}

	/**
	 * @return aquisi��o do tempo em milissegundos que o sistema est� ligado.
	 */

	public long getUptime()
	{
		return System.currentTimeMillis() - started;
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
		tick = new TimerTick(1, Long.MAX_VALUE);
	}

	/**
	 * Termina o sistema limpando a lista e indexa��o dos temporizadores.
	 */

	public void terminate()
	{
		timers.clear();

		free();
	}

	/**
	 * O Sistema de Temporizadores utiliza o Padr�o de Projetos Singleton.
	 * Por esse motivo � necess�rio a exist�ncia desse m�todo para obt�-lo.
	 * @return aquisi��o da �nica inst�ncia do Sistema de Temporizadores.
	 */

	public static TimerSystem getInstance()
	{
		return INSTANCE;
	}
}

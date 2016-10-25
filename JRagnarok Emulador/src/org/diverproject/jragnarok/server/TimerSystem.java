package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.free;

import org.diverproject.util.TimerTick;
import org.diverproject.util.collection.Map;
import org.diverproject.util.lang.IntUtil;

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
	 * Tempo mínimo de um temporizador.
	 */
	public static final int MIN_TIMER_INTERVAL = 20;

	/**
	 * Tempo limite de um temporizador.
	 */
	public static final int MAX_TIMER_INTERVAL = 1000;


	/**
	 * Única instância disponível do Sistema de Temporizadores.
	 */
	private static final TimerSystem INSTANCE = new TimerSystem();


	/**
	 * Quando o sistema foi inicializado.
	 */
	private long started;

	/**
	 * Quando ocorreu o último tick no sistema.
	 */
	private int lastTick;

	/**
	 * Quantos ticks (milissegundos) já se passaram desde a inicialização.
	 */
	private int lastTickCount;

	/**
	 * Referência do objeto que faz a cronometragem do sistema.
	 */
	private TimerTick tick;

	/**
	 * Mapeador de temporizadores.
	 */
	private TimerMap timers;

	/**
	 * Cria um novo Sistema de Temporizadores definindo o mapa de temporizadores e listeners.
	 * Também inicializa o cronômetro com TimerTick definindo o intervalo de 1 milissegundo.
	 */

	private TimerSystem()
	{
		tick = new TimerTick(1);
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
	 * Cria um novo temporizador e o adiciona ao mapa de temporizadores.
	 * O código de identificação dele é auto incremental no sistema.
	 * @return aquisição do objeto temporizador para ser utilizado.
	 */

	public Timer acquireTimer()
	{
		Timer timer = new Timer();
		timers.add(timer);

		return timer;
	}

	/**
	 * Aumenta o tempo de um temporizador em milissegundos especificados.
	 * @param timer referência do temporizador que será atualizado.
	 * @param milisseconds quantidade de tempo em milissegundos.
	 */

	public void addTickTimer(Timer timer, int milisseconds)
	{
		setTickTimer(timer, timer.getTick() + milisseconds);
	}

	/**
	 * Define o tempo de um temporizador em milissegundos especificados.
	 * @param timer referência do temporizador que será atualizado.
	 * @param tick tempo do servidor em milissegundos ligado.
	 */

	private void setTickTimer(Timer timer, int tick)
	{
		if (tick < 0)
			tick = 0;

		timer.setTick(tick);
	}

	/**
	 * A realização do tick permite obter a quantidade de milissegundos passados.
	 * Essa contagem é feita a partir da última chamada desse mesmo método.
	 * Portanto se a primeira chamada é feita em 100 e a segunda em 110 o tick é 10.
	 * @return aquisição de quantos milissegundos se passaram desde o último tick.
	 */

	public int tick()
	{
		lastTick = (int) tick.getTicks();
		lastTickCount = (int) tick.getTicksCount();

		return lastTickCount;
	}

	/**
	 * O último tick representa a diferença de tempo entre o último e penúltimo tick.
	 * @return aquisição da quantidade de milissegundos calculados no último tick.
	 */

	public int getLastTick()
	{
		return lastTick;
	}

	/**
	 * A contagem de ticks é salva no sistema para que possa ser usado se necessário.
	 * @return aquisição do tempo em milissegundos já calculados em ticks.
	 */

	public int getLastTickCount()
	{
		return lastTickCount;
	}

	/**
	 * Atualiza o sistema de temporizadores afim de executá-los se necessário.
	 * @param tick momento no tempo do sistema para atualizar os temporizadores.
	 * @return tempo em milissegundos para o próximo temporizador vencer,
	 * irá considerar os valores limites de MIN_TIMER_INTERVAL e MAX_TIMER_INTERVAL.
	 */

	public int update(int tick)
	{
		int diff = timers.update(tick);

		return IntUtil.limit(diff, MIN_TIMER_INTERVAL, MAX_TIMER_INTERVAL);
	}

	/**
	 * @return aquisição do tempo em milissegundos que o sistema está ligado.
	 */

	public long getUptime()
	{
		return System.currentTimeMillis() - started;
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
		tick = new TimerTick(1, Long.MAX_VALUE);
	}

	/**
	 * Termina o sistema limpando a lista e indexação dos temporizadores.
	 */

	public void terminate()
	{
		timers.clear();

		free();
	}

	/**
	 * O Sistema de Temporizadores utiliza o Padrão de Projetos Singleton.
	 * Por esse motivo é necessário a existência desse método para obtê-lo.
	 * @return aquisição da única instância do Sistema de Temporizadores.
	 */

	public static TimerSystem getInstance()
	{
		return INSTANCE;
	}
}

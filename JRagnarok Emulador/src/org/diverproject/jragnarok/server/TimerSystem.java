package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.free;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.util.TimerTick;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.Map.MapItem;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;
import org.diverproject.util.collection.abstraction.StringSimpleMap;
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
	 * Mapa contendo os temporizadores que foram adicionados.
	 */
	private Map<Integer, Timer> timers;

	/**
	 * Mapa contendo os temporizadores em ordem de execução.
	 */
	private Tickers tickers;

	/**
	 * Mapa contendo os listeners dos temporizados.
	 */
	private Map<String, TimerListener> listeners;

	/**
	 * Cria um novo Sistema de Temporizadores definindo o mapa de temporizadores e listeners.
	 * Também inicializa o cronômetro com TimerTick definindo o intervalo de 1 milissegundo.
	 */

	private TimerSystem()
	{
		tick = new TimerTick(1);
		timers = new IntegerLittleMap<>();
		tickers = new Tickers();
		listeners = new StringSimpleMap<>();
	}

	/**
	 * Adiciona um novo listener a lista de listeners dos temporizadores.
	 * @param listener referência do listener que deseja adicionar.
	 * @param name nome que será vinculado a essa listener.
	 */

	public void addListener(TimerListener listener, String name)
	{
		if (listener != null && name != null)
		{
			if (listeners.containsKey(name))
				logWarning("função duplicada (name: %s)", name);
			else
				listeners.add(name, listener);
		}
	}

	/**
	 * Procura o nome de um listener através da sua própria referência.
	 * @param listener referência do listener do qual deseja o nome.
	 * @return aquisição do nome desse listener se for encontrado.
	 */

	public String searchListener(TimerListener listener)
	{
		for (MapItem<String, TimerListener> item : listeners.iterateItems())
			if (item.value.equals(listener))
				return item.key;

		return "timer listener desconhecido";
	}

	/**
	 * Cria um novo temporizador e o adiciona ao mapa de temporizadores.
	 * O código de identificação dele é auto incremental no sistema.
	 * @return aquisição do objeto temporizador para ser utilizado.
	 */

	public Timer acquireTimer()
	{
		Timer timer = new Timer();
		timers.add(timer.getID(), timer);

		return timer;
	}

	/**
	 * Atualiza um temporizador para que será excluído assim que expirado.
	 * @param timer referência do temporizador que deseja alterar.
	 * @param tick tempo para executar novamente esse temporizador.
	 * @param listener referência do listener contendo a função do temporizador.
	 * @param data TODO what is that?
	 */

	public void addTimer(Timer timer, int tick, TimerListener listener, int data)
	{
		if (!timers.contains(timer))
			timers.add(timer.getID(), timer);

		timer.setTick(tick);
		timer.setData(data);
		timer.setInterval(1000);
		timer.setListener(listener);
		timer.getType().set(Timer.TIMER_ONCE_AUTODEL);

		tickers.update(timer);
	}

	/**
	 * Atualiza um temporizador para auto reiniciar quando for expirado.
	 * Assim sendo deverá ser removido manualmente do sistema.
	 * @param timer referência do temporizador que será atualizado.
	 * @param tick tempo para executar novamente esse temporizador.
	 * @param listener referência do listener contendo a função do temporizador.
	 * @param data TODO what is that?
	 * @param interval intervalo para que o temporizador seja renovado.
	 */

	public void addInterval(Timer timer, int tick, TimerListener listener, int data, int interval)
	{
		if (interval < 1)
			logError("intervalo inválido (tick: %d, listener: %s, id: %d, data: %d).\n", tick, searchListener(listener), timer.getID(), data);

		else
		{
			if (!timers.contains(timer))
				timers.add(timer.getID(), timer);

			timer.setTick(tick);
			timer.setData(data);
			timer.setInterval(interval);
			timer.setListener(listener);
			timer.getType().set(Timer.TIMER_INTERVAL);

			tickers.update(timer);
		}
	}

	/**
	 * Permite remover um determinado temporizador do sistema de temporizadores.
	 * Será removido ainda o listener vinculado a essa temporizador se houver.
	 * @param timer referência do temporizador que será removido do sistema.
	 */

	public void delete(Timer timer)
	{
		if (timer.getListener() != null)
			listeners.removeKey(searchListener(timer.getListener()));

		timer.setListener(null);
		timer.getType().set(Timer.TIMER_ONCE_AUTODEL);
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
	 * Executa todos os temporizadores que tiverem o seu tempo expirado no sistema.
	 * @param tick momento no tempo do sistema para atualizar os temporizadores.
	 * @return tempo em milissegundos para o próximo temporizador vencer,
	 * irá considerar os valores limites de MIN_TIMER_INTERVAL e MAX_TIMER_INTERVAL.
	 */

	public int update(int tick)
	{
		int diff = MAX_TIMER_INTERVAL;

		synchronized (tickers)
		{
			for (Timer timer : tickers)
			{
				diff = timer.getTick() - tick;

				if (diff > 0)
					break;

				timer.getType().set(Timer.TIMER_REMOVE);

				if (timer.getListener() != null)
				{
					if (diff < -MAX_TIMER_INTERVAL)
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
							tickers.remove(timer);
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
		}

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
		tickers.clear();

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

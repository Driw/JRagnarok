package org.diverproject.jragnarok.server;

import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Temporizador</h1>
 *
 * <p>Um temporizador funciona como uma espécie de contagem regressiva.
 * Além de ser uma contagem regressiva está vinculado a um listener.
 * Esse listener terá um método que será chamado ao fim da contagem.</p>
 *
 * <p>Um temporizador possui um tempo (tick) em que este será expirado.
 * Após ser expirado, irá chamar o método especificado no listener.
 * Em seguida se for de loop restabelece a contagem regressiva.
 * Caso contrário irá removê-lo do sitema, pois se executa uma vez.</p>
 *
 * @see TimerListener
 * @see BitWise
 *
 * @author Andrew
 */

public class Timer implements Comparable<Timer>
{
	/**
	 * Contagem auto incremental para distinguir os temporizadores.
	 */
	private static int autoIncrement;


	/**
	 * Código de identificação do temporizador.
	 */
	private int id;

	/**
	 * Código de identificação de um objeto vinculado.
	 */
	private int objectID;

	/**
	 * Quando o temporizador irá expirar.
	 */
	private int tick;

	/**
	 * Intervalo em ticks para atualizar o tempo de expiração.
	 */
	private int interval;

	/**
	 * Tipo do temporizador.
	 */
	private TimerType type;

	/**
	 * Listener contendo o método a ser executado.
	 */
	private TimerListener listener;

	/**
	 * Cria um novo temporizador definindo a sua identificação.
	 * Inicializa o tipo do temporizador, carregando o nome das opções.
	 */

	Timer()
	{
		id = ++autoIncrement;
		type = TimerType.TIMER_INVALID;
	}

	@Override
	public int compareTo(Timer timer)
	{
		if (id == timer.id)
			return 0;

		int diff = tick - timer.tick;

		return diff == 0 ? 1 : diff;
	}

	/**
	 * Cada temporizador instanciado possui um código único no sistema.
	 * @return aquisição do código de identificação do temporizador.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * Afim de permitir que o temporizador trabalha relativamente com um objeto específico,
	 * deve ser definido um código para a identificação desse objeto em questão.
	 * @return aquisição do código de identificação do objeto que será trabalhado.
	 */

	public int getObjectID()
	{
		return objectID;
	}

	/**
	 * Afim de permitir que o temporizador trabalha relativamente com um objeto específico,
	 * deve ser definido um código para a identificação desse objeto em questão.
	 * @param objectID código de identificação do objeto que será trabalhado.
	 */

	public void setObjectID(int objectID)
	{
		if (objectID > 0)
			this.objectID = objectID;
	}

	/**
	 * Tick é um valor em intervalo de tempo usado como cronômetro.
	 * Para o temporizador irá indicar quando ele deve expirar.
	 * @return aquisição do tempo de expiração do temporizador.
	 */

	public int getTick()
	{
		return tick;
	}

	/**
	 * Tick é um valor em intervalo de tempo usado como cronômetro.
	 * Permite definir quando o temporizador será expirado.
	 * @param tick momento no tempo para expiração.
	 */

	public void setTick(int tick)
	{
		if (tick > 0)
			this.tick = tick;
	}

	/**
	 * Intervalo é usado para se saber quantos ticks serão incrementados.
	 * O incremento só irá ocorrer pós expiração e se for do tipo loop.
	 * @return valor em ticks que serão incrementados na expiração.
	 */

	public int getInterval()
	{
		return interval;
	}

	/**
	 * Intervalo é usado para se saber quantos ticks serão incrementados.
	 * O incremento só irá ocorrer pós expiração e se for do tipo loop.
	 * @param interval valor em ticks que serão incrementados.
	 */

	void setInterval(int interval)
	{
		if (interval > 0)
			this.interval = interval;
	}

	/**
	 * O tipo de temporizador determina como será o seu comportamento no sistema.
	 * @return aquisição do tipo de comportamento do temporizador.
	 */

	public TimerType getType()
	{
		return type;
	}

	/**
	 * O tipo de temporizador determina como será o seu comportamento no sistema.
	 * @param type novo tipo de comportamento do temporizador.
	 */

	void setType(TimerType type)
	{
		if (type != null)
			this.type = type;
	}

	/**
	 * Listener para o temporizador é usado para definir o que será feito.
	 * Sempre que for expirado irá chamar o listener e executar o seu método.
	 * @return aquisição do listener que será executado quando expirado.
	 */

	TimerListener getListener()
	{
		return listener;
	}

	/**
	 * Listener para o temporizador é usado para definir o que será feito.
	 * Sempre que for expirado irá chamar o listener e executar o seu método.
	 * @param listener listener que deverá ser executado quando expirado.
	 */

	public void setListener(TimerListener listener)
	{
		this.listener = listener;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("tick", tick);
		description.append("interval", interval);
		description.append("type", type);
		description.append("listener", listener.getName());

		return description.toString();
	}
}

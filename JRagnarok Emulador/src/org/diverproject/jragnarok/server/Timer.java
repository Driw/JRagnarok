package org.diverproject.jragnarok.server;

import org.diverproject.util.BitWise;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Temporizador</h1>
 *
 * <p>Um temporizador funciona como uma esp�cie de contagem regressiva.
 * Al�m de ser uma contagem regressiva est� vinculado a um listener.
 * Esse listener ter� um m�todo que ser� chamado ao fim da contagem.</p>
 *
 * <p>Um temporizador possui um tempo (tick) em que este ser� expirado.
 * Ap�s ser expirado, ir� chamar o m�todo especificado no listener.
 * Em seguida se for de loop restabelece a contagem regressiva.
 * Caso contr�rio ir� remov�-lo do sitema, pois se executa uma vez.</p>
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
	 * C�digo de identifica��o do temporizador.
	 */
	private int id;

	/**
	 * C�digo de identifica��o de um objeto vinculado.
	 */
	private int objectID;

	/**
	 * Quando o temporizador ir� expirar.
	 */
	private int tick;

	/**
	 * Intervalo em ticks para atualizar o tempo de expira��o.
	 */
	private int interval;

	/**
	 * Tipo do temporizador.
	 */
	private TimerType type;

	/**
	 * Listener contendo o m�todo a ser executado.
	 */
	private TimerListener listener;

	/**
	 * Cria um novo temporizador definindo a sua identifica��o.
	 * Inicializa o tipo do temporizador, carregando o nome das op��es.
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
	 * Cada temporizador instanciado possui um c�digo �nico no sistema.
	 * @return aquisi��o do c�digo de identifica��o do temporizador.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * Afim de permitir que o temporizador trabalha relativamente com um objeto espec�fico,
	 * deve ser definido um c�digo para a identifica��o desse objeto em quest�o.
	 * @return aquisi��o do c�digo de identifica��o do objeto que ser� trabalhado.
	 */

	public int getObjectID()
	{
		return objectID;
	}

	/**
	 * Afim de permitir que o temporizador trabalha relativamente com um objeto espec�fico,
	 * deve ser definido um c�digo para a identifica��o desse objeto em quest�o.
	 * @param objectID c�digo de identifica��o do objeto que ser� trabalhado.
	 */

	public void setObjectID(int objectID)
	{
		if (objectID > 0)
			this.objectID = objectID;
	}

	/**
	 * Tick � um valor em intervalo de tempo usado como cron�metro.
	 * Para o temporizador ir� indicar quando ele deve expirar.
	 * @return aquisi��o do tempo de expira��o do temporizador.
	 */

	public int getTick()
	{
		return tick;
	}

	/**
	 * Tick � um valor em intervalo de tempo usado como cron�metro.
	 * Permite definir quando o temporizador ser� expirado.
	 * @param tick momento no tempo para expira��o.
	 */

	public void setTick(int tick)
	{
		if (tick > 0)
			this.tick = tick;
	}

	/**
	 * Intervalo � usado para se saber quantos ticks ser�o incrementados.
	 * O incremento s� ir� ocorrer p�s expira��o e se for do tipo loop.
	 * @return valor em ticks que ser�o incrementados na expira��o.
	 */

	public int getInterval()
	{
		return interval;
	}

	/**
	 * Intervalo � usado para se saber quantos ticks ser�o incrementados.
	 * O incremento s� ir� ocorrer p�s expira��o e se for do tipo loop.
	 * @param interval valor em ticks que ser�o incrementados.
	 */

	void setInterval(int interval)
	{
		if (interval > 0)
			this.interval = interval;
	}

	/**
	 * O tipo de temporizador determina como ser� o seu comportamento no sistema.
	 * @return aquisi��o do tipo de comportamento do temporizador.
	 */

	public TimerType getType()
	{
		return type;
	}

	/**
	 * O tipo de temporizador determina como ser� o seu comportamento no sistema.
	 * @param type novo tipo de comportamento do temporizador.
	 */

	void setType(TimerType type)
	{
		if (type != null)
			this.type = type;
	}

	/**
	 * Listener para o temporizador � usado para definir o que ser� feito.
	 * Sempre que for expirado ir� chamar o listener e executar o seu m�todo.
	 * @return aquisi��o do listener que ser� executado quando expirado.
	 */

	TimerListener getListener()
	{
		return listener;
	}

	/**
	 * Listener para o temporizador � usado para definir o que ser� feito.
	 * Sempre que for expirado ir� chamar o listener e executar o seu m�todo.
	 * @param listener listener que dever� ser executado quando expirado.
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

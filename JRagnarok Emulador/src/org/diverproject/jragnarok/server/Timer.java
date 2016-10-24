package org.diverproject.jragnarok.server;

import org.diverproject.util.BitWise;

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

public class Timer
{
	/**
	 * C�digo que define o tipo para funcionar uma vez e ser exclu�do.
	 */
	public static final int TIMER_ONCE_AUTODEL = 0x01;

	/**
	 * C�digo que define o tipo para funcionar como loops.
	 */
	public static final int TIMER_INTERVAL = 0x02;

	/**
	 * C�digo que define o tipo como expirado (remover).
	 */
	public static final int TIMER_REMOVE = 0x10;

	/**
	 * Valor para considerar temporizador inv�lido.
	 */
	public static final int INVALID_TIMER = -1;

	/**
	 * Vetor contendo o nome de todos os tipos de temporizadores.
	 */
	private static final String TIMER_STRINGS[] = new String[]
	{ "ONCE_AUTODEL", "INTERVAL", "0x04", "0x08", "REMOVE" };


	/**
	 * Contagem auto incremental para distinguir os temporizadores.
	 */
	private static int autoIncrement;


	/**
	 * C�digo de identifica��o do temporizador.
	 */
	private int id;

	/**
	 * Quando o temporizador ir� expirar.
	 */
	private int tick;

	/**
	 * Intervalo em ticks para atualizar o tempo de expira��o.
	 */
	private int interval;

	/**
	 * TODO what is that?
	 */
	private int data;

	/**
	 * Tipo do temporizador.
	 */
	private BitWise type;

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
		type = new BitWise(TIMER_STRINGS);
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

	public void setInterval(int interval)
	{
		this.interval = interval;
	}

	/**
	 * TODO what is that?
	 * @return
	 */

	public int getData()
	{
		return data;
	}

	/**
	 * TODO what is that?
	 * @param data
	 */

	public void setData(int data)
	{
		this.data = data;
	}

	/**
	 * O tipo de temporizador ir� definir como ele ir� funcionar internamente no sistema.
	 * Segue os tipos de temporizadores e seus comportamentos dentro do sistema:
	 * TIMER_ONCE_AUTODEL ir� executar uma vez e ser exclu�do;
	 * TIMER_INTERVAL ser� executado e incrementa o tempo de expira��o (loop);
	 * TIMER_REMOVE remove automaticamente o temporizador sem execut�-lo.
	 * @return aquisi��o de um objeto que permite definir o tipo de temporizador.
	 */

	BitWise getType()
	{
		return type;
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

	void setListener(TimerListener listener)
	{
		this.listener = listener;
	}
}

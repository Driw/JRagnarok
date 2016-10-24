package org.diverproject.jragnarok.server;

import org.diverproject.util.BitWise;

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

public class Timer
{
	/**
	 * Código que define o tipo para funcionar uma vez e ser excluído.
	 */
	public static final int TIMER_ONCE_AUTODEL = 0x01;

	/**
	 * Código que define o tipo para funcionar como loops.
	 */
	public static final int TIMER_INTERVAL = 0x02;

	/**
	 * Código que define o tipo como expirado (remover).
	 */
	public static final int TIMER_REMOVE = 0x10;

	/**
	 * Valor para considerar temporizador inválido.
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
	 * Código de identificação do temporizador.
	 */
	private int id;

	/**
	 * Quando o temporizador irá expirar.
	 */
	private int tick;

	/**
	 * Intervalo em ticks para atualizar o tempo de expiração.
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
		type = new BitWise(TIMER_STRINGS);
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
	 * O tipo de temporizador irá definir como ele irá funcionar internamente no sistema.
	 * Segue os tipos de temporizadores e seus comportamentos dentro do sistema:
	 * TIMER_ONCE_AUTODEL irá executar uma vez e ser excluído;
	 * TIMER_INTERVAL será executado e incrementa o tempo de expiração (loop);
	 * TIMER_REMOVE remove automaticamente o temporizador sem executá-lo.
	 * @return aquisição de um objeto que permite definir o tipo de temporizador.
	 */

	BitWise getType()
	{
		return type;
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

	void setListener(TimerListener listener)
	{
		this.listener = listener;
	}
}

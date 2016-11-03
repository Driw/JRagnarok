package org.diverproject.jragnarok.server;

public enum TimerType
{
	/**
	 * Temporizador inv�lido.
	 */
	TIMER_INVALID,

	/**
	 * Temporizador pronto para ser removido.
	 */
	TIMER_REMOVE,

	/**
	 * Temporizador que ser� executado somente uma vez e na pr�xima atualiza��o.
	 */
	TIMER_ONCE_AUTODEL,

	/**
	 * Temporizador que ser� executado somente uma vez ap�s tantos milissegundos.
	 */
	TIMER_INTERVAL,

	/**
	 * Temporizador que ser� executado diversas vezes ap�s tantos milissegundos.
	 */
	TIMER_LOOP,
}

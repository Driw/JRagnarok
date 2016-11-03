package org.diverproject.jragnarok.server;

public enum TimerType
{
	/**
	 * Temporizador inválido.
	 */
	TIMER_INVALID,

	/**
	 * Temporizador pronto para ser removido.
	 */
	TIMER_REMOVE,

	/**
	 * Temporizador que será executado somente uma vez e na próxima atualização.
	 */
	TIMER_ONCE_AUTODEL,

	/**
	 * Temporizador que será executado somente uma vez após tantos milissegundos.
	 */
	TIMER_INTERVAL,

	/**
	 * Temporizador que será executado diversas vezes após tantos milissegundos.
	 */
	TIMER_LOOP,
}

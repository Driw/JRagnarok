package org.diverproject.jragnarok.server;

/**
 * Listener para Temporizador
 *
 * Listener usado para executar comandos/operações de um temporizador.
 * Eles são vinculados ao temporizadores para definir a ação do mesmo.
 *
 * @author Andrew
 */

public interface TimerListener
{
	/**
	 * Procedimento chamado internamente pelo Sistema de Temporizadores.
	 * A chamada ocorre somente quando o tempo do temporizador timer expirado.
	 * @param timer referência do temporizador que detêm esse listener.
	 * @param tick momento em que essa chama está sendo feita no sistema.
	 */

	void onCall(Timer timer, int tick);
}

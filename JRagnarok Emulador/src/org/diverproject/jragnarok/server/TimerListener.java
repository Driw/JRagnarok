package org.diverproject.jragnarok.server;

/**
 * Listener para Temporizador
 *
 * Listener usado para executar comandos/opera��es de um temporizador.
 * Eles s�o vinculados ao temporizadores para definir a a��o do mesmo.
 *
 * @author Andrew
 */

public interface TimerListener
{
	/**
	 * Procedimento chamado internamente pelo Sistema de Temporizadores.
	 * A chamada ocorre somente quando o tempo do temporizador timer expirado.
	 * @param timer refer�ncia do temporizador que det�m esse listener.
	 * @param tick momento em que essa chama est� sendo feita no sistema.
	 */

	void onCall(Timer timer, int tick);

	/**
	 * O nome dos listeners devem ser �nicos e especificar sua finalidade.
	 * @return aquisi��o do nome do listener para ser localizado.
	 */

	String getName();
}

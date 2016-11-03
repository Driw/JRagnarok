package org.diverproject.jragnarok.server;

/**
 * <h1>Listener para Temporizador</h1>
 *
 * <p>Listener usado para executar comandos/opera��es de um temporizador.
 * Eles s�o vinculados ao temporizadores para definir a a��o do mesmo.</p>
 *
 * @see Timer
 *
 * @author Andrew
 */

public interface TimerListener
{
	/**
	 * Procedimento chamado internamente pelo Sistema de Temporizadores.
	 * A chamada ocorre somente quando o tempo do temporizador timer expirado.
	 * @param now tempo atual que ocorre a atualiza��o em milissegundos.
	 * @param tick milissegundos passados desde a �ltima atualiza��o.
	 */

	void onCall(Timer timer, int now, int tick);

	/**
	 * O nome dos listeners devem ser �nicos e especificar sua finalidade.
	 * @return aquisi��o do nome do listener para ser localizado.
	 */

	String getName();
}

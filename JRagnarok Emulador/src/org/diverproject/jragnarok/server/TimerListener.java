package org.diverproject.jragnarok.server;

/**
 * <h1>Listener para Temporizador</h1>
 *
 * <p>Listener usado para executar comandos/operações de um temporizador.
 * Eles são vinculados ao temporizadores para definir a ação do mesmo.</p>
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
	 * @param now tempo atual que ocorre a atualização em milissegundos.
	 * @param tick milissegundos passados desde a última atualização.
	 */

	void onCall(Timer timer, int now, int tick);

	/**
	 * O nome dos listeners devem ser únicos e especificar sua finalidade.
	 * @return aquisição do nome do listener para ser localizado.
	 */

	String getName();
}

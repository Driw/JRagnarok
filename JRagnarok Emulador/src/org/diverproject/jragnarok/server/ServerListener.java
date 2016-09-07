package org.diverproject.jragnarok.server;

import org.diverproject.jragnaork.RagnarokException;

/**
 * <h1>Listener de Servidor</h1>
 *
 * <p>Usado por todos os servidores para executarem opera��es ap�s a mudan�a de estado.
 * Essas opera��es precisam ser executados em momentos espec�ficos conforme o estado do mesmo.</p>
 *
 * @author Andrew
 */

public interface ServerListener
{
	/**
	 * Chamado quando for solicitado a cria��o do servidor.
	 * Dever� garantir que todos os objetos sejam inicializados.
	 * @throws RagnarokException falha durante a solicita��o.
	 */

	void onCreate() throws RagnarokException;

	/**
	 * Chamado somente ap�s a finaliza��o da cria��o do servidor.
	 * Dever� garantir que os servi�os sejam inicializados.
	 * @throws RagnarokException falha durante a cria��o.
	 */

	void onCreated() throws RagnarokException;

	/**
	 * Chamado quando for solicitado a inicializa��o do servidor.
	 * Dever� garantir que os servidores sejam interligados.
	 * @throws RagnarokException falha durante a inicializa��o.
	 */

	void onRunning() throws RagnarokException;

	/**
	 * Chamado quando for solicitado a parada do servidor.
	 * Dever� garantir que objetos sejam interrompidos.
	 * @throws RagnarokException falha durante a solicita��o.
	 */

	void onStop() throws RagnarokException;

	/**
	 * Chamado quando o servidor j� tiver sido parado.
	 * Dever� garantir que os servi�os sejam interrompidos.
	 * @throws RagnarokException falha durante a parada.
	 */

	void onStoped() throws RagnarokException;

	/**
	 * Chamado quando for solicitado para destruir o servidor.
	 * Dever� garantir que as conex�es foram fechadas e avisadas.
	 * @throws RagnarokException falha durante a solicita��o.
	 */

	void onDestroy() throws RagnarokException;

	/**
	 * Chamado somente quando o servidor j� tiver sido destru�do.
	 * Liberar todos os objetos em mem�ria para fechar com seguran�a.
	 * @throws RagnarokException falha durante a destrui��o.
	 */

	void onDestroyed() throws RagnarokException;
}

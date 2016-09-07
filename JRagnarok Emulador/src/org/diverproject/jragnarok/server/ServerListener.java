package org.diverproject.jragnarok.server;

import org.diverproject.jragnaork.RagnarokException;

/**
 * <h1>Listener de Servidor</h1>
 *
 * <p>Usado por todos os servidores para executarem operações após a mudança de estado.
 * Essas operações precisam ser executados em momentos específicos conforme o estado do mesmo.</p>
 *
 * @author Andrew
 */

public interface ServerListener
{
	/**
	 * Chamado quando for solicitado a criação do servidor.
	 * Deverá garantir que todos os objetos sejam inicializados.
	 * @throws RagnarokException falha durante a solicitação.
	 */

	void onCreate() throws RagnarokException;

	/**
	 * Chamado somente após a finalização da criação do servidor.
	 * Deverá garantir que os serviços sejam inicializados.
	 * @throws RagnarokException falha durante a criação.
	 */

	void onCreated() throws RagnarokException;

	/**
	 * Chamado quando for solicitado a inicialização do servidor.
	 * Deverá garantir que os servidores sejam interligados.
	 * @throws RagnarokException falha durante a inicialização.
	 */

	void onRunning() throws RagnarokException;

	/**
	 * Chamado quando for solicitado a parada do servidor.
	 * Deverá garantir que objetos sejam interrompidos.
	 * @throws RagnarokException falha durante a solicitação.
	 */

	void onStop() throws RagnarokException;

	/**
	 * Chamado quando o servidor já tiver sido parado.
	 * Deverá garantir que os serviços sejam interrompidos.
	 * @throws RagnarokException falha durante a parada.
	 */

	void onStoped() throws RagnarokException;

	/**
	 * Chamado quando for solicitado para destruir o servidor.
	 * Deverá garantir que as conexões foram fechadas e avisadas.
	 * @throws RagnarokException falha durante a solicitação.
	 */

	void onDestroy() throws RagnarokException;

	/**
	 * Chamado somente quando o servidor já tiver sido destruído.
	 * Liberar todos os objetos em memória para fechar com segurança.
	 * @throws RagnarokException falha durante a destruição.
	 */

	void onDestroyed() throws RagnarokException;
}

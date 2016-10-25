package org.diverproject.jragnarok.server;

/**
 * <h1>Estados de Servidor</h1>
 *
 * <p>Enumeração contendo todos os estados que um servidor pode se encontrar.
 * Cada estado é obtido conforme a solicitação para suas mudanças por método.
 * Assim é possível que determinadas operações sejam executadas nessas mudanças.</p>
 *
 * @author Andrew
 */

public enum ServerState
{
	/**
	 * Instanciado.
	 */
	NONE,

	/**
	 * Criação concluída.
	 */
	CREATED,

	/**
	 * Rodando.
	 */
	RUNNING,

	/**
	 * parada
	 */
	STOPED,

	/**
	 * Destruição concluída.
	 */
	DESTROYED,
}

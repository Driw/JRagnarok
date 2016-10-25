package org.diverproject.jragnarok.server;

/**
 * <h1>Estados de Servidor</h1>
 *
 * <p>Enumera��o contendo todos os estados que um servidor pode se encontrar.
 * Cada estado � obtido conforme a solicita��o para suas mudan�as por m�todo.
 * Assim � poss�vel que determinadas opera��es sejam executadas nessas mudan�as.</p>
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
	 * Cria��o conclu�da.
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
	 * Destrui��o conclu�da.
	 */
	DESTROYED,
}

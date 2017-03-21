package org.diverproject.jragnarok.server.login.entities;

/**
 * <h1>Interface de Acessos</h1>
 *
 * <p>Interface utilizada por classes (representa uma conta) que precisam realizar opera��es no banco de dados.
 * Atrav�s dessa interface � especificado um m�todo que permite obter o seu c�digo de identifica��o.</p>
 *
 * @author Andrew
 */

public interface Login
{
	/**
	 * @return aquisi��o do c�digo de identifica��o da conta no banco de dados.
	 */

	public int getID();
}

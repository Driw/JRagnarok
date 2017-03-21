package org.diverproject.jragnarok.server.login.entities;

/**
 * <h1>Interface de Acessos</h1>
 *
 * <p>Interface utilizada por classes (representa uma conta) que precisam realizar operações no banco de dados.
 * Através dessa interface é especificado um método que permite obter o seu código de identificação.</p>
 *
 * @author Andrew
 */

public interface Login
{
	/**
	 * @return aquisição do código de identificação da conta no banco de dados.
	 */

	public int getID();
}

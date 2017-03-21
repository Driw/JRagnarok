package org.diverproject.jragnarok.server.common;

import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.util.ObjectDescription;

/**
 * Acesso Adaptado
 *
 * <p>Classe que implementa a interface para identifica��o do acesso de uma conta no banco de dados.
 * Ir� implementar o atributo e o m�todo relacionado a esse atributo referente ao seu c�digo de identifica��o.
 * Desta forma todas as classes que representem uma conta no sistema poder�o ser originados de uma mesma fonte.</p>
 *
 * @see Login
 *
 * @author Andrew
 */

public class LoginAdapter implements Login
{
	/**
	 * C�digo de identifica��o da conta.
	 */
	private int accountID;

	/**
	 * Cria uma nova inst�ncia de um acesso adaptado sendo necess�rio definir a conta.
	 * @param accountID c�digo de identifica��o da conta que est� sendo utilizada.
	 */

	public LoginAdapter(int accountID)
	{
		this.accountID = accountID;
	}

	@Override
	public int getID()
	{
		return accountID;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);

		return description.toString();
	}
}

package org.diverproject.jragnarok.server.common;

import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.util.ObjectDescription;

/**
 * Acesso Adaptado
 *
 * <p>Classe que implementa a interface para identificação do acesso de uma conta no banco de dados.
 * Irá implementar o atributo e o método relacionado a esse atributo referente ao seu código de identificação.
 * Desta forma todas as classes que representem uma conta no sistema poderão ser originados de uma mesma fonte.</p>
 *
 * @see Login
 *
 * @author Andrew
 */

public class LoginAdapter implements Login
{
	/**
	 * Código de identificação da conta.
	 */
	private int accountID;

	/**
	 * Cria uma nova instância de um acesso adaptado sendo necessário definir a conta.
	 * @param accountID código de identificação da conta que está sendo utilizada.
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

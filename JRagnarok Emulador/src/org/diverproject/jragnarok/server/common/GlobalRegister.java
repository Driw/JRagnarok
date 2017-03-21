package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Registro Global</h1>
 *
 * <p>Classe que al�m de representar um registro (vari�vel do jogo) o vincula a uma conta especificada.
 * Assim sendo ser� necess�rio definir o c�digo de identifica��o da conta do qual det�m/deter� o registro.
 * Lembrando que a conta e nome da vari�vel n�o poder�o ser alterados ap�s serem definidos no construtor.</p>
 *
 * @see Register
 *
 * @author Andrew
 *
 * @param <E>
 */

public class GlobalRegister<E> extends Register<E>
{
	/**
	 * C�digo de identifica��o da conta que det�m o registro.
	 */
	private int accountID;

	/**
	 * Cria uma nova inst�ncia de um registro global (vari�vel do jogo) que � vinculado a uma conta.
	 * @param accountID c�digo de identifica��o da conta do qual ir� deter este registro.
	 * @param key nome de identifica��o �nico do registro na conta especificada.
	 */

	public GlobalRegister(int accountID, String key)
	{
		super(key);

		this.accountID = accountID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o da conta que det�m o registro.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	@Override
	public void toString(ObjectDescription description)
	{
		description.append("accountID", accountID);

		super.toString(description);
	}
}

package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Registro Global</h1>
 *
 * <p>Classe que além de representar um registro (variável do jogo) o vincula a uma conta especificada.
 * Assim sendo será necessário definir o código de identificação da conta do qual detém/deterá o registro.
 * Lembrando que a conta e nome da variável não poderão ser alterados após serem definidos no construtor.</p>
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
	 * Código de identificação da conta que detém o registro.
	 */
	private int accountID;

	/**
	 * Cria uma nova instância de um registro global (variável do jogo) que é vinculado a uma conta.
	 * @param accountID código de identificação da conta do qual irá deter este registro.
	 * @param key nome de identificação único do registro na conta especificada.
	 */

	public GlobalRegister(int accountID, String key)
	{
		super(key);

		this.accountID = accountID;
	}

	/**
	 * @return aquisição do código de identificação da conta que detém o registro.
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

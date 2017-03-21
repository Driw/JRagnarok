package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

/**
 * Opera��o para Registro Global
 *
 * Classe utilizada para guardar informa��es de registros globais que devem ser alterados no banco de dados.
 * Ser� especificado um objeto contendo o registro(nome e valor respectivamente) 
 *
 * @author Andrew
 *
 * @param <E>
 */

public class GlobalRegisterOperation<E>
{
	/**
	 * Opera��o para atualizar um valor inteiro.
	 */
	public static final int OPERATION_INT_REPLACE = 0;

	/**
	 * Opera��o para excluir um valor inteiro.
	 */
	public static final int OPERATION_INT_DELETE = 1;

	/**
	 * Opera��o para atualizar um valor em string.
	 */
	public static final int OPERATION_STR_REPLACE = 2;

	/**
	 * Opera��o para excluir um valor em string.
	 */
	public static final int OPERATION_STR_DELETE = 3;


	/**
	 * C�digo da opera��o do qual ser� realizada com o registro em quest�o.
	 */
	private int operation;

	/**
	 * Refer�ncia do registro do qual deseja realizar a opera��o.
	 */
	private GlobalRegister<E> register;
	
	/**
	 * @return aquisi��o do c�digo da opera��o desejada (<code>OPERATION_*</code>).
	 */

	public int getOperation()
	{
		return operation;
	}

	/**
	 * @param operation c�digo da nova opera��o desejada (<code>OPERATION_*</code>).
	 */

	public void setOperation(int operation)
	{
		this.operation = operation;
	}

	/**
	 * @return aquisi��o da refer�ncia do registro que ir� sofrer a opera��o.
	 */

	public GlobalRegister<E> getRegister()
	{
		return register;
	}

	/**
	 * @param register refer�ncia do registro que ir� sofrer a opera��o.
	 */

	public void setRegister(GlobalRegister<E> register)
	{
		this.register = register;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		if (register != null)
			register.toString(description);

		description.append("operation", operation);

		return description.toString();
	}
}

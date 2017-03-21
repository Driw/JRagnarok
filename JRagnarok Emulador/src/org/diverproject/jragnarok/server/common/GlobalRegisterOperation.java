package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

/**
 * Operação para Registro Global
 *
 * Classe utilizada para guardar informações de registros globais que devem ser alterados no banco de dados.
 * Será especificado um objeto contendo o registro(nome e valor respectivamente) 
 *
 * @author Andrew
 *
 * @param <E>
 */

public class GlobalRegisterOperation<E>
{
	/**
	 * Operação para atualizar um valor inteiro.
	 */
	public static final int OPERATION_INT_REPLACE = 0;

	/**
	 * Operação para excluir um valor inteiro.
	 */
	public static final int OPERATION_INT_DELETE = 1;

	/**
	 * Operação para atualizar um valor em string.
	 */
	public static final int OPERATION_STR_REPLACE = 2;

	/**
	 * Operação para excluir um valor em string.
	 */
	public static final int OPERATION_STR_DELETE = 3;


	/**
	 * Código da operação do qual será realizada com o registro em questão.
	 */
	private int operation;

	/**
	 * Referência do registro do qual deseja realizar a operação.
	 */
	private GlobalRegister<E> register;
	
	/**
	 * @return aquisição do código da operação desejada (<code>OPERATION_*</code>).
	 */

	public int getOperation()
	{
		return operation;
	}

	/**
	 * @param operation código da nova operação desejada (<code>OPERATION_*</code>).
	 */

	public void setOperation(int operation)
	{
		this.operation = operation;
	}

	/**
	 * @return aquisição da referência do registro que irá sofrer a operação.
	 */

	public GlobalRegister<E> getRegister()
	{
		return register;
	}

	/**
	 * @param register referência do registro que irá sofrer a operação.
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

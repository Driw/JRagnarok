package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.RagnarokRuntimeException;

/**
 * <h1>Tipo do Cliente</h1>
 *
 * <p>Classe com a enumera��o dos tipos de clientes aceitos pelo sistema JRagnarok.
 * FIXME: At� o momento n�o se sabe a especifica��o e influ�ncia desse valor no sistema.</p>
 *
 * @author Andrew
 */

public enum ClientType
{
	/**
	 * Tipo de cliente n�o definido (desconhecido).
	 */
	CT_NONE(0),

	/**
	 * Tipo padr�o do cliente que est� sendo utilizado (�nico aceito).
	 */
	CT_DEFAULT(22);

	/**
	 * C�digo de identifica��o do tipo de cliente recebido do cliente.
	 */

	public final byte CODE;

	/**
	 * Cria uma nova inst�ncia de uma enumera��o para definir um tipo de cliente no sistema.
	 * @param code c�digo de identifica��o do tipo de cliente recebido do cliente.
	 */

	private ClientType(int code)
	{
		CODE = b(code);
	}

	/**
	 * Procedimento que permite realizar a an�lise de um c�digo especificado e obter o tipo do cliente.
	 * @param b c�digo de identifica��o do tipo de cliente do qual deseja selecionar a enumera��o.
	 * @return aquisi��o da enumera��o do tipo de cliente conforme o seu c�digo de identifica��o.
	 */

	public static ClientType parse(byte b)
	{
		for (ClientType type : values())
			if (type.CODE == b)
				return type;

		throw new RagnarokRuntimeException("%d n�o � um ClientType", b);
	}
}

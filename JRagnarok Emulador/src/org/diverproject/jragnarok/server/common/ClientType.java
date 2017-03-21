package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.RagnarokRuntimeException;

/**
 * <h1>Tipo do Cliente</h1>
 *
 * <p>Classe com a enumeração dos tipos de clientes aceitos pelo sistema JRagnarok.
 * FIXME: Até o momento não se sabe a especificação e influência desse valor no sistema.</p>
 *
 * @author Andrew
 */

public enum ClientType
{
	/**
	 * Tipo de cliente não definido (desconhecido).
	 */
	CT_NONE(0),

	/**
	 * Tipo padrão do cliente que está sendo utilizado (único aceito).
	 */
	CT_DEFAULT(22);

	/**
	 * Código de identificação do tipo de cliente recebido do cliente.
	 */

	public final byte CODE;

	/**
	 * Cria uma nova instância de uma enumeração para definir um tipo de cliente no sistema.
	 * @param code código de identificação do tipo de cliente recebido do cliente.
	 */

	private ClientType(int code)
	{
		CODE = b(code);
	}

	/**
	 * Procedimento que permite realizar a análise de um código especificado e obter o tipo do cliente.
	 * @param b código de identificação do tipo de cliente do qual deseja selecionar a enumeração.
	 * @return aquisição da enumeração do tipo de cliente conforme o seu código de identificação.
	 */

	public static ClientType parse(byte b)
	{
		for (ClientType type : values())
			if (type.CODE == b)
				return type;

		throw new RagnarokRuntimeException("%d não é um ClientType", b);
	}
}

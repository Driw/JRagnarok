package org.diverproject.jragnarok.server.login;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Hash para Cliente</h1>
 *
 * <p>No sistema um hash para cliente é usado como uma forma de identificar e validar um cliente.
 * Através da identificação é possível saber se este cliente está disponível para ser usado.
 * E após a identificação será validado se o mesmo poderá ser usado pela conta acessada.</p>
 *
 * <p>O cliente pode sofrer diversas modificações devido a atualizações/correções necessárias.
 * Assim sendo, pode ser necessário um identificador específico do mesmo que muda até mesmo
 * caso terceiros tenham feito alguma modificação no cliente executável evitando seu uso.</p>
 *
 * @author Andrew
 */

public class ClientHash
{
	/**
	 * Quantidade de bytes que terá cada hash para cliente.
	 */
	public static final int SIZE = 16;


	/**
	 * Vetor contendo os bytes do hash do cliente.
	 */
	private final byte hash[];

	/**
	 * String criada para visualização do hash gerado.
	 */
	private String hashString;

	/**
	 * Cria uma nova instância de um hash para cliente inicializando o vetor de bytes do hash.
	 */

	public ClientHash()
	{
		hash = new byte[16];
	}

	/**
	 * Permite definir um novo valor para este hash, o hash deve ser de <code>SIZE</code> bytes.
	 * @param hashValue vetor contendo os bytes que será usado para definir este hash.
	 */

	public void set(byte[] hashValue)
	{
		if (hashValue.length == SIZE)
		{
			for (int i = 0; i < hashValue.length; i++)
				hash[i] = hashValue[i];

			hashString = new String(hash);
		}
	}

	/**
	 * @return aquisição da string gerada ao definir o valor do hash.
	 */

	public String getHashString()
	{
		return hashString;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof ClientHash)
		{
			ClientHash clientHash = (ClientHash) obj;

			for (int i = 0; i < SIZE; i++)
			{
				if (hash[i] != clientHash.hash[i])
					return false;

				if (hash[i] == 0)
					break;
			}

			return true;
		}

		return false;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append(getHashString());

		return description.toString();
	}
}

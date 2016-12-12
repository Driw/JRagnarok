package org.diverproject.jragnarok.server.login;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Hash para Cliente</h1>
 *
 * <p>No sistema um hash para cliente � usado como uma forma de identificar e validar um cliente.
 * Atrav�s da identifica��o � poss�vel saber se este cliente est� dispon�vel para ser usado.
 * E ap�s a identifica��o ser� validado se o mesmo poder� ser usado pela conta acessada.</p>
 *
 * <p>O cliente pode sofrer diversas modifica��es devido a atualiza��es/corre��es necess�rias.
 * Assim sendo, pode ser necess�rio um identificador espec�fico do mesmo que muda at� mesmo
 * caso terceiros tenham feito alguma modifica��o no cliente execut�vel evitando seu uso.</p>
 *
 * @author Andrew
 */

public class ClientHash
{
	/**
	 * Quantidade de bytes que ter� cada hash para cliente.
	 */
	public static final int SIZE = 16;


	/**
	 * Vetor contendo os bytes do hash do cliente.
	 */
	private final byte hash[];

	/**
	 * String criada para visualiza��o do hash gerado.
	 */
	private String hashString;

	/**
	 * Cria uma nova inst�ncia de um hash para cliente inicializando o vetor de bytes do hash.
	 */

	public ClientHash()
	{
		hash = new byte[16];
	}

	/**
	 * Permite definir um novo valor para este hash, o hash deve ser de <code>SIZE</code> bytes.
	 * @param hashValue vetor contendo os bytes que ser� usado para definir este hash.
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
	 * @return aquisi��o da string gerada ao definir o valor do hash.
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

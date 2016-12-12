package org.diverproject.jragnarok.server.common;

import static org.diverproject.jragnarok.JRagnarokUtil.random;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Seed de Acesso</h1>
 *
 * <p>Através da seed é possível identificar se um acesso está sendo usado pela conexão correta.
 * Isso evita que o possível acesso de duas contas no sistema cause conflito de informações.
 * A seed de acesso funciona quase como um hash para identificar cada acesso no sistema.</p>
 *
 * @see CanCopy
 *
 * @author Andrew
 */

public class LoginSeed implements CanCopy<LoginSeed>
{
	/**
	 * Primeira seed.
	 */
	private int first;

	/**
	 * Segunda seed.
	 */
	private int second;

	/**
	 * @return aquisição da primeira seed.
	 */

	public int getFirst()
	{
		return first;
	}

	/**
	 * Gera uma nova primeira seed aleatório.
	 */

	public void genFirst()
	{
		first = random() + 1;
	}

	/**
	 * @return aquisição da segunda seed.
	 */

	public int getSecond()
	{
		return second;
	}

	/**
	 * Gera uma nova segunda seed aleatório.
	 */

	public void genSecond()
	{
		second = random() + 1;
	}

	/**
	 * Permite definir o valor das seed (usado quando recebido de um outro servidor).
	 * @param firstSeed valor respectivo a primeira seed gerada.
	 * @param secondSeed valor respectivo a segunda seed gerada.
	 */

	public void set(int firstSeed, int secondSeed)
	{
		first = firstSeed;
		second = secondSeed;
	}

	/**
	 * Verifica se duas seeds passadas a seguir correspondem as seed desse acesso.
	 * @param firstSeed valor respectivo a primeira seed gerada à verificar.
	 * @param secondSeed valor respectivo a segunda seed gerada à verificar.
	 * @return true se ambas as seed forem iguais ou false caso contrário.
	 */

	public boolean equals(int firstSeed, int secondSeed)
	{
		return first == firstSeed && second == secondSeed;
	}

	@Override
	public void copyFrom(LoginSeed seed)
	{
		first = seed.first;
		second = seed.second;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof LoginSeed)
		{
			LoginSeed seed = (LoginSeed) object;

			return	seed.first == first &&
					seed.second == second;
		}

		return false;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("first", first);
		description.append("second", second);

		return description.toString();
	}
}

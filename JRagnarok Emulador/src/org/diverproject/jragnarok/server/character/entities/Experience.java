package org.diverproject.jragnarok.server.character.entities;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Experi�ncia</h1>
 *
 * <p>Objeto que armazena as informa��es referentes ao n�vel de experi�ncia de um personagem.
 * Os dois principais valores deste objeto � sobre o n�vel de base e o n�vel de classe.
 * As experi�ncias podem ser trabalhadas de formas diferentes conforme o necess�rio.</p>
 *
 * <p>No caso da experi�ncia de base e classe, n�o s�o acumulativas ap�s o personagem evoluir.
 * J� para a experi�ncia de fama n�o h� n�vel, apenas quantidade de experi�ncia obtida.
 * Estas s�o as duas formas de se poder trabalhar com a experi�ncia de um personagem.</p>
 *
 * @author Andrew
 */

public class Experience implements CanCopy<Experience>
{
	/**
	 * Quantidade de experi�ncia no n�vel de base atual.
	 */
	private int base;

	/**
	 * Quantidade de experi�ncia no n�vel de classe atual.
	 */
	private int job;

	/**
	 * Quantidade de fama do personagem.
	 */
	private int fame;

	/**
	 * @return aquisi��o da quantidade de experi�ncia no n�vel de base atual.
	 */

	public int getBase()
	{
		return base;
	}

	/**
	 * @param base quantidade de experi�ncia no n�vel de base atual.
	 */

	public void setBase(int base)
	{
		this.base = base;
	}

	/**
	 * @return aquisi��o da quantidade de experi�ncia no n�vel de classe atual.
	 */

	public int getJob()
	{
		return job;
	}

	/**
	 * @param job quantidade de experi�ncia no n�vel de classe atual.
	 */

	public void setJob(int job)
	{
		this.job = job;
	}

	/**
	 * @return aquisi��o da quantidade de fama do personagem.
	 */

	public int getFame()
	{
		return fame;
	}

	/**
	 * @param fame quantidade de fama do personagem.
	 */

	public void setFame(int fame)
	{
		this.fame = fame;
	}

	@Override
	public void copyFrom(Experience e)
	{
		if (e != null)
		{
			base = e.base;
			job = e.job;
			fame = e.fame;
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("base", base);
		description.append("job", job);
		description.append("fame", fame);

		return description.toString();
	}
}

package org.diverproject.jragnarok.server.character.entities;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Experiência</h1>
 *
 * <p>Objeto que armazena as informações referentes ao nível de experiência de um personagem.
 * Os dois principais valores deste objeto é sobre o nível de base e o nível de classe.
 * As experiências podem ser trabalhadas de formas diferentes conforme o necessário.</p>
 *
 * <p>No caso da experiência de base e classe, não são acumulativas após o personagem evoluir.
 * Já para a experiência de fama não há nível, apenas quantidade de experiência obtida.
 * Estas são as duas formas de se poder trabalhar com a experiência de um personagem.</p>
 *
 * @author Andrew
 */

public class Experience implements CanCopy<Experience>
{
	/**
	 * Quantidade de experiência no nível de base atual.
	 */
	private int base;

	/**
	 * Quantidade de experiência no nível de classe atual.
	 */
	private int job;

	/**
	 * Quantidade de fama do personagem.
	 */
	private int fame;

	/**
	 * @return aquisição da quantidade de experiência no nível de base atual.
	 */

	public int getBase()
	{
		return base;
	}

	/**
	 * @param base quantidade de experiência no nível de base atual.
	 */

	public void setBase(int base)
	{
		this.base = base;
	}

	/**
	 * @return aquisição da quantidade de experiência no nível de classe atual.
	 */

	public int getJob()
	{
		return job;
	}

	/**
	 * @param job quantidade de experiência no nível de classe atual.
	 */

	public void setJob(int job)
	{
		this.job = job;
	}

	/**
	 * @return aquisição da quantidade de fama do personagem.
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

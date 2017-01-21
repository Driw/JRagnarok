package org.diverproject.jragnarok.server.common.entities;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Família</h1>
 *
 * <p>Uma família possui informações para identificação dos membros de parentescos de um personagem.
 * Para cada personagem será possível ter apenas: um conjugue, um pai, uma mãe e um filho.
 * A família é identificada através do código de identificação dos personagens em questão.</p>
 *
 * <p>Caso o personagem não possua um pai/mãe e não possua um filho deve ser especificado.
 * Para estes casos o próprio método já indica que os valores para tal são 0 (zero).</p>
 *
 * @author Andrew
 */

public class Family implements CanCopy<Family>
{
	/**
	 * ID (personagem) do conjugue deste personagem.
	 */
	private int partner;

	/**
	 * ID (personagem) do pai deste personagem.
	 */
	private int father;

	/**
	 * ID (personagem) da mãe deste personagem.
	 */
	private int mother;

	/**
	 * ID (personagem) do filho deste personagem.
	 */
	private int child;

	/**
	 * @return aquisição do id do personagem que é o conjugue deste personagem.
	 */

	public int getPartner()
	{
		return partner;
	}

	/**
	 * @param partner id do personagem que é o conjugue deste personagem.
	 */

	public void setPartner(int partner)
	{
		this.partner = partner;
	}

	/**
	 * @return aquisição do id do personagem do pai deste personagem (0: não tem).
	 */

	public int getFather()
	{
		return father;
	}

	/**
	 * @param father id do personagem do pai deste personagem (0: não tem).
	 */

	public void setFather(int father)
	{
		this.father = father;
	}

	/**
	 * @return aquisição do id do personagem da mãe deste personagem (0: não tem).
	 */

	public int getMother()
	{
		return mother;
	}

	/**
	 * @param mother id do personagem da mãe deste personagem (0: não tem).
	 */

	public void setMother(int mother)
	{
		this.mother = mother;
	}

	/**
	 * @return aquisição do id do personagem do filho deste personagem (0: não tem).
	 */

	public int getChild()
	{
		return child;
	}

	/**
	 * @param child id do personagem do filho deste personagem (0: não tem).
	 */

	public void setChild(int child)
	{
		this.child = child;
	}

	@Override
	public void copyFrom(Family e)
	{
		if (e != null)
		{
			partner = e.partner;
			father = e.father;
			mother = e.mother;
			child = e.child;
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("partner", partner);
		description.append("father", father);
		description.append("mother", mother);
		description.append("child", child);

		return description.toString();
	}
}

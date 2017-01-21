package org.diverproject.jragnarok.server.common.entities;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Fam�lia</h1>
 *
 * <p>Uma fam�lia possui informa��es para identifica��o dos membros de parentescos de um personagem.
 * Para cada personagem ser� poss�vel ter apenas: um conjugue, um pai, uma m�e e um filho.
 * A fam�lia � identificada atrav�s do c�digo de identifica��o dos personagens em quest�o.</p>
 *
 * <p>Caso o personagem n�o possua um pai/m�e e n�o possua um filho deve ser especificado.
 * Para estes casos o pr�prio m�todo j� indica que os valores para tal s�o 0 (zero).</p>
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
	 * ID (personagem) da m�e deste personagem.
	 */
	private int mother;

	/**
	 * ID (personagem) do filho deste personagem.
	 */
	private int child;

	/**
	 * @return aquisi��o do id do personagem que � o conjugue deste personagem.
	 */

	public int getPartner()
	{
		return partner;
	}

	/**
	 * @param partner id do personagem que � o conjugue deste personagem.
	 */

	public void setPartner(int partner)
	{
		this.partner = partner;
	}

	/**
	 * @return aquisi��o do id do personagem do pai deste personagem (0: n�o tem).
	 */

	public int getFather()
	{
		return father;
	}

	/**
	 * @param father id do personagem do pai deste personagem (0: n�o tem).
	 */

	public void setFather(int father)
	{
		this.father = father;
	}

	/**
	 * @return aquisi��o do id do personagem da m�e deste personagem (0: n�o tem).
	 */

	public int getMother()
	{
		return mother;
	}

	/**
	 * @param mother id do personagem da m�e deste personagem (0: n�o tem).
	 */

	public void setMother(int mother)
	{
		this.mother = mother;
	}

	/**
	 * @return aquisi��o do id do personagem do filho deste personagem (0: n�o tem).
	 */

	public int getChild()
	{
		return child;
	}

	/**
	 * @param child id do personagem do filho deste personagem (0: n�o tem).
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

package org.diverproject.jragnarok.server.character.structures;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.lang.ShortUtil;

/**
 * Atributos de Personagem
 *
 * Classe respons�vel por identificar a quantidade de pontos distribu�dos em cada atributo do personagem.
 * S�o 6 atributos dispon�veis que s�o: for�a, agilidade, vitalidade, intelig�ncia, destreza e sorte.
 * Todos os atributos devem possuir um valor m�nimo de 1 e um limite m�ximo de 32767.
 *
 * @author Andrew
 */

public class Stats implements CanCopy<Stats>
{
	/**
	 * Quantidade m�nima de pontos distribu�dos em um atributo.
	 */
	private static final short MIN_STAT = 1;

	/**
	 * Quantidade m�xima de pontos distribu�dos em um atributo.
	 */
	private static final short MAX_STAT = 32767;


	/**
	 * Pontos distribu�dos em for�a.
	 */
	private short strength;

	/**
	 * Pontos distribu�dos em agilidade.
	 */
	private short agility;

	/**
	 * Pontos distribu�dos em vitalidade.
	 */
	private short vitality;

	/**
	 * Pontos distribu�dos em intelig�ncia.
	 */
	private short intelligence;

	/**
	 * Pontos distribu�dos em destreza.
	 */
	private short dexterity;

	/**
	 * Pontos distribu�dos em sorte.
	 */
	private short luck;

	/**
	 * @return aquisi��o de pontos distribu�dos em for�a.
	 */

	public short getStrength()
	{
		return strength;
	}

	/**
	 * @param strength pontos distribu�dos em for�a.
	 */

	public void setStrength(short strength)
	{
		this.strength = ShortUtil.limit(strength, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em agilidade.
	 */

	public short getAgility()
	{
		return agility;
	}

	/**
	 * @param agility pontos distribu�dos em agilidade.
	 */

	public void setAgility(short agility)
	{
		this.agility = ShortUtil.limit(agility, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em vitalidade.
	 */

	public short getVitality()
	{
		return vitality;
	}

	/**
	 * @param vitality pontos distribu�dos em vitalidade.
	 */

	public void setVitality(short vitality)
	{
		this.vitality = ShortUtil.limit(vitality, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em intelig�ncia.
	 */

	public short getIntelligence()
	{
		return intelligence;
	}

	/**
	 * @param intelligence pontos distribu�dos em intelig�ncia.
	 */

	public void setIntelligence(short intelligence)
	{
		this.intelligence = ShortUtil.limit(intelligence, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em destreza.
	 */

	public short getDexterity()
	{
		return dexterity;
	}

	/**
	 * @param dexterity pontos distribu�dos em destreza.
	 */

	public void setDexterity(short dexterity)
	{
		this.dexterity = ShortUtil.limit(dexterity, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisi��o de pontos distribu�dos em sorte.
	 */

	public short getLuck()
	{
		return luck;
	}

	/**
	 * @param luck pontos distribu�dos em sorte.
	 */

	public void setLuck(short luck)
	{
		this.luck = ShortUtil.limit(luck, MIN_STAT, MAX_STAT);
	}

	@Override
	public void copyFrom(Stats e)
	{
		if (e != null)
		{
			strength = e.strength;
			agility = e.agility;
			vitality = e.vitality;
			intelligence = e.intelligence;
			dexterity = e.dexterity;
			luck = e.luck;
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("str", strength);
		description.append("agi", agility);
		description.append("vit", vitality);
		description.append("int", intelligence);
		description.append("dex", dexterity);
		description.append("luk", luck);

		return description.toString();
	}
}

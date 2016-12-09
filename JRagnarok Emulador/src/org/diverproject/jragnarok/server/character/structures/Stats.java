package org.diverproject.jragnarok.server.character.structures;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.lang.ShortUtil;

/**
 * Atributos de Personagem
 *
 * Classe responsável por identificar a quantidade de pontos distribuídos em cada atributo do personagem.
 * São 6 atributos disponíveis que são: força, agilidade, vitalidade, inteligência, destreza e sorte.
 * Todos os atributos devem possuir um valor mínimo de 1 e um limite máximo de 32767.
 *
 * @author Andrew
 */

public class Stats implements CanCopy<Stats>
{
	/**
	 * Quantidade mínima de pontos distribuídos em um atributo.
	 */
	private static final short MIN_STAT = 1;

	/**
	 * Quantidade máxima de pontos distribuídos em um atributo.
	 */
	private static final short MAX_STAT = 32767;


	/**
	 * Pontos distribuídos em força.
	 */
	private short strength;

	/**
	 * Pontos distribuídos em agilidade.
	 */
	private short agility;

	/**
	 * Pontos distribuídos em vitalidade.
	 */
	private short vitality;

	/**
	 * Pontos distribuídos em inteligência.
	 */
	private short intelligence;

	/**
	 * Pontos distribuídos em destreza.
	 */
	private short dexterity;

	/**
	 * Pontos distribuídos em sorte.
	 */
	private short luck;

	/**
	 * @return aquisição de pontos distribuídos em força.
	 */

	public short getStrength()
	{
		return strength;
	}

	/**
	 * @param strength pontos distribuídos em força.
	 */

	public void setStrength(short strength)
	{
		this.strength = ShortUtil.limit(strength, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisição de pontos distribuídos em agilidade.
	 */

	public short getAgility()
	{
		return agility;
	}

	/**
	 * @param agility pontos distribuídos em agilidade.
	 */

	public void setAgility(short agility)
	{
		this.agility = ShortUtil.limit(agility, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisição de pontos distribuídos em vitalidade.
	 */

	public short getVitality()
	{
		return vitality;
	}

	/**
	 * @param vitality pontos distribuídos em vitalidade.
	 */

	public void setVitality(short vitality)
	{
		this.vitality = ShortUtil.limit(vitality, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisição de pontos distribuídos em inteligência.
	 */

	public short getIntelligence()
	{
		return intelligence;
	}

	/**
	 * @param intelligence pontos distribuídos em inteligência.
	 */

	public void setIntelligence(short intelligence)
	{
		this.intelligence = ShortUtil.limit(intelligence, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisição de pontos distribuídos em destreza.
	 */

	public short getDexterity()
	{
		return dexterity;
	}

	/**
	 * @param dexterity pontos distribuídos em destreza.
	 */

	public void setDexterity(short dexterity)
	{
		this.dexterity = ShortUtil.limit(dexterity, MIN_STAT, MAX_STAT);
	}

	/**
	 * @return aquisição de pontos distribuídos em sorte.
	 */

	public short getLuck()
	{
		return luck;
	}

	/**
	 * @param luck pontos distribuídos em sorte.
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

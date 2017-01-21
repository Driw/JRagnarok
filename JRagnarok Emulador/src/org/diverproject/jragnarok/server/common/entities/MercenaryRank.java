package org.diverproject.jragnarok.server.common.entities;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Classificação de Assistentes</h1>
 *
 * <p>Essa classificação indica a quantidade de chamados e confiança dos tipos de assistentes.
 * Cada tipo de assistente irá receber um valor referente a confiança e chamados.
 * Quanto maior o nível de confiança do assistente maior será a sua efetividade.</p>
 *
 * <p>Para cada assistente recrutado será adicionado em um o chamado daquele tipo de assistente.
 * Todo assistente possui um tempo de duração (contrato) com o jogador que o escolheu.
 * A confiança do assistente aumenta proporcionalmente ao tempo que ficou vivo durante o contrato.</p>
 *
 * @author Andrew
 */

public class MercenaryRank implements CanCopy<MercenaryRank>
{
	/**
	 * Nível de confiança dos assistentes arqueiros.
	 */
	private int archerFaith;

	/**
	 * Quantidade de assistentes arqueiros já contratados.
	 */
	private int archerCalls;

	/**
	 * Nível de confiança dos assistentes lanceiros.
	 */
	private int spearFaith;

	/**
	 * Quantidade de assistentes lanceiros já contratados.
	 */
	private int spearCalls;

	/**
	 * Nível de confiança dos assistentes espadachins.
	 */
	private int swordFaith;

	/**
	 * Quantidade de assistentes espadachins já contratados.
	 */
	private int swordCalls;

	/**
	 * @return aquisição do nível de confiança dos assistentes arqueiros.
	 */

	public int getArcherFaith()
	{
		return archerFaith;
	}

	/**
	 * @param archerFaith nível de confiança dos assistentes arqueiros.
	 */

	public void setArcherFaith(int archerFaith)
	{
		this.archerFaith = archerFaith;
	}

	/**
	 * @return aquisição da quantidade de assistentes arqueiros já contratados.
	 */

	public int getArcherCalls()
	{
		return archerCalls;
	}

	/**
	 * @param archerCalls quantidade de assistentes arqueiros já contratados.
	 */

	public void setArcherCalls(int archerCalls)
	{
		this.archerCalls = archerCalls;
	}

	/**
	 * @return aquisição do nível de confiança dos assistentes lanceiros.
	 */

	public int getSpearFaith()
	{
		return spearFaith;
	}

	/**
	 * @param spearFaith nível de confiança dos assistentes lanceiros.
	 */

	public void setSpearFaith(int spearFaith)
	{
		this.spearFaith = spearFaith;
	}

	/**
	 * @return aquisição da quantidade de assistentes lanceiros já contratados.
	 */

	public int getSpearCalls()
	{
		return spearCalls;
	}

	/**
	 * @param spearCalls quantidade de assistentes lanceiros já contratados.
	 */

	public void setSpearCalls(int spearCalls)
	{
		this.spearCalls = spearCalls;
	}

	/**
	 * @return aquisição do nível de confiança dos assistentes espadachins.
	 */

	public int getSwordFaith()
	{
		return swordFaith;
	}

	/**
	 * @param swordFaith nível de confiança dos assistentes espadachins.
	 */

	public void setSwordFaith(int swordFaith)
	{
		this.swordFaith = swordFaith;
	}

	/**
	 * @return aquisição da quantidade de assistentes espadachins já contratados.
	 */

	public int getSwordCalls()
	{
		return swordCalls;
	}

	/**
	 * @param swordCalls quantidade de assistentes espadachins já contratados.
	 */

	public void setSwordCalls(int swordCalls)
	{
		this.swordCalls = swordCalls;
	}

	@Override
	public void copyFrom(MercenaryRank e)
	{
		if (e != null)
		{
			archerFaith = e.archerFaith;
			archerCalls = e.archerCalls;
			spearFaith = e.spearFaith;
			spearCalls = e.spearCalls;
			swordFaith = e.swordFaith;
			swordCalls = e.swordCalls;
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("archer", format("%d/%d", archerFaith, archerCalls));
		description.append("spear", format("%d/%d", spearFaith, spearCalls));
		description.append("sword", format("%d/%d", swordFaith, swordCalls));

		return description.toString();
	}
}

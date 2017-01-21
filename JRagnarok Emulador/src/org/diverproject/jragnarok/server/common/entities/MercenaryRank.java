package org.diverproject.jragnarok.server.common.entities;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import org.diverproject.util.CanCopy;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Classifica��o de Assistentes</h1>
 *
 * <p>Essa classifica��o indica a quantidade de chamados e confian�a dos tipos de assistentes.
 * Cada tipo de assistente ir� receber um valor referente a confian�a e chamados.
 * Quanto maior o n�vel de confian�a do assistente maior ser� a sua efetividade.</p>
 *
 * <p>Para cada assistente recrutado ser� adicionado em um o chamado daquele tipo de assistente.
 * Todo assistente possui um tempo de dura��o (contrato) com o jogador que o escolheu.
 * A confian�a do assistente aumenta proporcionalmente ao tempo que ficou vivo durante o contrato.</p>
 *
 * @author Andrew
 */

public class MercenaryRank implements CanCopy<MercenaryRank>
{
	/**
	 * N�vel de confian�a dos assistentes arqueiros.
	 */
	private int archerFaith;

	/**
	 * Quantidade de assistentes arqueiros j� contratados.
	 */
	private int archerCalls;

	/**
	 * N�vel de confian�a dos assistentes lanceiros.
	 */
	private int spearFaith;

	/**
	 * Quantidade de assistentes lanceiros j� contratados.
	 */
	private int spearCalls;

	/**
	 * N�vel de confian�a dos assistentes espadachins.
	 */
	private int swordFaith;

	/**
	 * Quantidade de assistentes espadachins j� contratados.
	 */
	private int swordCalls;

	/**
	 * @return aquisi��o do n�vel de confian�a dos assistentes arqueiros.
	 */

	public int getArcherFaith()
	{
		return archerFaith;
	}

	/**
	 * @param archerFaith n�vel de confian�a dos assistentes arqueiros.
	 */

	public void setArcherFaith(int archerFaith)
	{
		this.archerFaith = archerFaith;
	}

	/**
	 * @return aquisi��o da quantidade de assistentes arqueiros j� contratados.
	 */

	public int getArcherCalls()
	{
		return archerCalls;
	}

	/**
	 * @param archerCalls quantidade de assistentes arqueiros j� contratados.
	 */

	public void setArcherCalls(int archerCalls)
	{
		this.archerCalls = archerCalls;
	}

	/**
	 * @return aquisi��o do n�vel de confian�a dos assistentes lanceiros.
	 */

	public int getSpearFaith()
	{
		return spearFaith;
	}

	/**
	 * @param spearFaith n�vel de confian�a dos assistentes lanceiros.
	 */

	public void setSpearFaith(int spearFaith)
	{
		this.spearFaith = spearFaith;
	}

	/**
	 * @return aquisi��o da quantidade de assistentes lanceiros j� contratados.
	 */

	public int getSpearCalls()
	{
		return spearCalls;
	}

	/**
	 * @param spearCalls quantidade de assistentes lanceiros j� contratados.
	 */

	public void setSpearCalls(int spearCalls)
	{
		this.spearCalls = spearCalls;
	}

	/**
	 * @return aquisi��o do n�vel de confian�a dos assistentes espadachins.
	 */

	public int getSwordFaith()
	{
		return swordFaith;
	}

	/**
	 * @param swordFaith n�vel de confian�a dos assistentes espadachins.
	 */

	public void setSwordFaith(int swordFaith)
	{
		this.swordFaith = swordFaith;
	}

	/**
	 * @return aquisi��o da quantidade de assistentes espadachins j� contratados.
	 */

	public int getSwordCalls()
	{
		return swordCalls;
	}

	/**
	 * @param swordCalls quantidade de assistentes espadachins j� contratados.
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

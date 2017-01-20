package org.diverproject.jragnarok.server.character;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Mudar Sexo</h1>
 *
 * <p>Objecto utilizado para carregar informações sobre a mudança de sexo de um personagem especificado.
 * As especificações consistem no código de identificação da conta e personagem, classe e clã.
 * Através das informações passadas será possível realizar as operações de troca do sexo do personagem</p>
 *
 * @author Andrew
 */

public class ChangeSex
{
	/**
	 * Código de identificação da conta.
	 */
	private int accountID;

	/**
	 * Código de identificação do personagem.
	 */
	private int charID;

	/**
	 * Código de identificação da classe.
	 */
	private int classID;

	/**
	 * Código de identificação do clã.
	 */
	private int guildID;

	/**
	 * @return aquisição do código de identificação da conta.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @param accountID código de identificação da conta.
	 */

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	/**
	 * @return aquisição do código de identificação do personagem.
	 */

	public int getCharID()
	{
		return charID;
	}

	/**
	 * @param charID código de identificação do personagem.
	 */

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	/**
	 * @return aquisição do código de identificação da classe.
	 */

	public int getClassID()
	{
		return classID;
	}

	/**
	 * @param classID código de identificação da classe.
	 */

	public void setClassID(int classID)
	{
		this.classID = classID;
	}

	/**
	 * @return aquisição do código de identificação do clã.
	 */

	public int getGuildID()
	{
		return guildID;
	}

	/**
	 * @param guildID código de identificação do clã.
	 */

	public void setGuildID(int guildID)
	{
		this.guildID = guildID;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);
		description.append("charID", charID);
		description.append("classID", classID);
		description.append("guilID", guildID);

		return description.toString();
	}
}

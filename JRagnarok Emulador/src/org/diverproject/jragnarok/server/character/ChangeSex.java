package org.diverproject.jragnarok.server.character;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Mudar Sexo</h1>
 *
 * <p>Objecto utilizado para carregar informa��es sobre a mudan�a de sexo de um personagem especificado.
 * As especifica��es consistem no c�digo de identifica��o da conta e personagem, classe e cl�.
 * Atrav�s das informa��es passadas ser� poss�vel realizar as opera��es de troca do sexo do personagem</p>
 *
 * @author Andrew
 */

public class ChangeSex
{
	/**
	 * C�digo de identifica��o da conta.
	 */
	private int accountID;

	/**
	 * C�digo de identifica��o do personagem.
	 */
	private int charID;

	/**
	 * C�digo de identifica��o da classe.
	 */
	private int classID;

	/**
	 * C�digo de identifica��o do cl�.
	 */
	private int guildID;

	/**
	 * @return aquisi��o do c�digo de identifica��o da conta.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @param accountID c�digo de identifica��o da conta.
	 */

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do personagem.
	 */

	public int getCharID()
	{
		return charID;
	}

	/**
	 * @param charID c�digo de identifica��o do personagem.
	 */

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o da classe.
	 */

	public int getClassID()
	{
		return classID;
	}

	/**
	 * @param classID c�digo de identifica��o da classe.
	 */

	public void setClassID(int classID)
	{
		this.classID = classID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do cl�.
	 */

	public int getGuildID()
	{
		return guildID;
	}

	/**
	 * @param guildID c�digo de identifica��o do cl�.
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

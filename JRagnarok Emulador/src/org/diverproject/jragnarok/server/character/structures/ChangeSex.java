package org.diverproject.jragnarok.server.character.structures;

import org.diverproject.util.ObjectDescription;

public class ChangeSex
{
	private int accountID;
	private int charID;
	private int classID;
	private int guildID;

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public int getCharID()
	{
		return charID;
	}

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	public int getClassID()
	{
		return classID;
	}

	public void setClassID(int classID)
	{
		this.classID = classID;
	}

	public int getGuildID()
	{
		return guildID;
	}

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

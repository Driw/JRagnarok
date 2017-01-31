package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.util.Util.strcap;

import org.diverproject.util.ObjectDescription;

public class TAG_CHARACTER_BLOCK_INFO
{
	public static final int BYTES = 24;
	public static final int UNBAN_SIZE = 20;

	private int charID;
	private String unbanTime;

	public int getCharID()
	{
		return charID;
	}

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	public String getUnbanTime()
	{
		return unbanTime;
	}

	public void setUnbanTime(String unbanTime)
	{
		this.unbanTime = strcap(unbanTime, UNBAN_SIZE);
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("charID", charID);
		description.append("unbanTime", unbanTime);

		return description.toString();
	}
}

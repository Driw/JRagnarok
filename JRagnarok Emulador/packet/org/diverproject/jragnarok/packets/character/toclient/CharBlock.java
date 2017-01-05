package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

public class CharBlock
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
}

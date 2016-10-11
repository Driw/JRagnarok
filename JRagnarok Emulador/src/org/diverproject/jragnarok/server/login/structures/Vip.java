package org.diverproject.jragnarok.server.login.structures;

public class Vip
{
	private int id;
	private String name;
	private byte charSlotCount;
	private short maxStorage;

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getCharSlotCount()
	{
		return charSlotCount;
	}

	public void setCharSlotCount(byte charSlotCount)
	{
		this.charSlotCount = charSlotCount;
	}

	public int getMaxStorage()
	{
		return maxStorage;
	}

	public void setMaxStorage(short maxStorage)
	{
		this.maxStorage = maxStorage;
	}
}

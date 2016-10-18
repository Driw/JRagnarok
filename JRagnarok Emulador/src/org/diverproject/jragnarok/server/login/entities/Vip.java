package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.util.ObjectDescription;

public class Vip
{
	private int id;
	private Group group;
	private String name;
	private byte charSlotCount;
	private short maxStorage;

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		if (id > 0)
			this.id = id;
	}

	public Group getGroup()
	{
		return group;
	}
	public void setGroup(Group group)
	{
		this.group = group;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = strcap(name, NAME_LENGTH);
	}

	public byte getCharSlotCount()
	{
		return charSlotCount;
	}

	public void setCharSlotCount(byte charSlotCount)
	{
		if (charSlotCount > 0)
			this.charSlotCount = charSlotCount;
	}

	public short getMaxStorage()
	{
		return maxStorage;
	}

	public void setMaxStorage(short maxStorage)
	{
		if (maxStorage > 0)
			this.maxStorage = maxStorage;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("name", name);
		description.append("charSlotCount", charSlotCount);
		description.append("maxStorage", maxStorage);

		return description.toString();
	}
}

package org.diverproject.jragnarok.server.login.entities;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class AccountGroup
{
	private int id;
	private Time timeout;
	private Group currentGroup;
	private Group oldGroup;
	private Vip vip;

	public AccountGroup()
	{
		timeout = new Time();
	}

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public Time getTime()
	{
		return timeout;
	}

	public boolean isOver()
	{
		return timeout.get() < System.currentTimeMillis();
	}

	public Group getCurrentGroup()
	{
		return currentGroup;
	}

	public void setCurrentGroup(Group currentGroup)
	{
		this.currentGroup = currentGroup;
	}

	public Group getOldGroup()
	{
		return oldGroup;
	}

	public void setOldGroup(Group oldGroup)
	{
		this.oldGroup = oldGroup;
	}

	public Vip getVip()
	{
		return vip;
	}

	public void setVip(Vip vip)
	{
		this.vip = vip;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		if (currentGroup != null)
			description.append("current", currentGroup.getName());

		if (oldGroup != null)
			description.append("old", oldGroup.getName());

		description.append("time", timeout);

		return description.toString();
	}
}

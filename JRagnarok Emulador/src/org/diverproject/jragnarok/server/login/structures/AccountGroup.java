package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class AccountGroup
{
	private Time time;
	private Group currentGroup;
	private Group oldGroup;
	private Vip vip;

	public AccountGroup()
	{
		time = new Time();
	}

	public Time getTime()
	{
		return time;
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

		description.append("time", time);

		return description.toString();
	}
}

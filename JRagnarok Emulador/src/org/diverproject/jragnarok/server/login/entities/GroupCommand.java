package org.diverproject.jragnarok.server.login.entities;

import org.diverproject.util.ObjectDescription;

public class GroupCommand
{
	private int id;
	private String name;

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

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof GroupCommand)
		{
			GroupCommand permission = (GroupCommand) obj;

			return permission.id == id;
		}

		return false;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("name", name);

		return description.toString();
	}
}

package org.diverproject.jragnarok.server.login.entities;

import org.diverproject.util.ObjectDescription;

public class Group
{
	private int id;
	private int level;
	private String name;
	private Group parent;
	private GroupCommands commands;
	private GroupPermissions permissions;
	private boolean logCommands;

	public Group()
	{
		commands = new GroupCommands();
		permissions = new GroupPermissions();
	}

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Group getParent()
	{
		return parent;
	}

	public void setParent(Group parent)
	{
		this.parent = parent;
	}

	public GroupCommands getCommands()
	{
		return commands;
	}

	public GroupPermissions getPermissions()
	{
		return permissions;
	}

	public boolean isLogCommands()
	{
		return logCommands;
	}

	public void setLogCommands(boolean logCommands)
	{
		this.logCommands = logCommands;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof Group)
		{
			Group group = (Group) obj;

			return group.id == id;
		}

		return false;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("level", level);
		description.append("name", name);
		description.append("commands", commands.size());
		description.append("permissions", permissions.size());

		if (parent != null)
			description.append("parent", parent.getName());

		return description.toString();
	}
}

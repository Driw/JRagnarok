package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.DynamicMap;

public class Group
{
	private int id;
	private int level;
	private String name;
	private Group parent;
	private Map<String, Boolean> commands;
	private Map<String, Boolean> permissions;
	private boolean logEnabled;

	public Group()
	{
		commands = new DynamicMap<>();
		permissions = new DynamicMap<>();
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

	public Map<String, Boolean> getCommands()
	{
		return commands;
	}

	public Map<String, Boolean> getPermissions()
	{
		return permissions;
	}

	public boolean isLogEnabled()
	{
		return logEnabled;
	}

	public void setLogEnabled(boolean logEnabled)
	{
		this.logEnabled = logEnabled;
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

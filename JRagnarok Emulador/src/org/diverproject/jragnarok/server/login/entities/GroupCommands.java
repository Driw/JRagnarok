package org.diverproject.jragnarok.server.login.entities;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

public class GroupCommands
{
	private Map<String, GroupCommand> commands;

	public GroupCommands()
	{
		commands = new StringSimpleMap<>();
	}

	public boolean add(GroupCommand command)
	{
		if (command != null)
			return commands.add(command.getName(), command);

		return false;
	}

	public boolean remove(GroupCommand command)
	{
		if (command != null)
			return commands.removeKey(command.getName());

		return false;
	}

	public boolean contains(String name)
	{
		return commands.containsKey(name);
	}

	public boolean contains(GroupCommand command)
	{
		if (command != null)
			return contains(command.getName());

		return false;
	}

	public int size()
	{
		return commands.size();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		for (GroupCommand command : commands)
			description.append(command.getName());

		return description.toString();
	}
}

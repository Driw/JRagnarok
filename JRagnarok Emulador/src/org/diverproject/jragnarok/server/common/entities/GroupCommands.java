package org.diverproject.jragnarok.server.common.entities;

import java.util.Iterator;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.Map.MapItem;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

public class GroupCommands implements Iterable<MapItem<String, Integer>>
{
	public static final String ENABLED_ALL = "all_commands";

	private boolean enabledAll;
	private Map<String, Integer> commands;

	public GroupCommands()
	{
		commands = new StringSimpleMap<>();
	}

	public void set(String command, int bitmask)
	{
		if (command != null)
		{
			if (command.equals(ENABLED_ALL))
				enabledAll = true;

			else
			{
				commands.removeKey(command);
				commands.add(command, bitmask);
			}
		}
	}

	public void remove(String command)
	{
		commands.removeKey(command);
	}

	public boolean contains(String command)
	{
		return commands.containsKey(command);
	}

	public boolean is(String command)
	{
		return enabledAll || commands.containsKey(command);
	}

	public boolean is(String command, int bitmask)
	{
		if (enabledAll)
			return true;

		Integer commandMask = commands.get(command);

		return commandMask != null && commandMask == bitmask;
	}

	public int size()
	{
		return commands.size();
	}

	@Override
	public Iterator<MapItem<String, Integer>> iterator()
	{
		return commands.iteratorItems();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		for (MapItem<String, Integer> command : commands.iterateItems())
			description.append(command.key, command.value);

		return description.toString();
	}
}

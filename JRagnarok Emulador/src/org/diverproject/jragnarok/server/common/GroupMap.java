package org.diverproject.jragnarok.server.common;

import java.util.Iterator;

import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

public class GroupMap implements Iterable<Group>
{
	private Map<Integer, Group> groups;

	public GroupMap()
	{
		groups = new IntegerLittleMap<>();
	}

	public Group get(int gid)
	{
		return groups.get(gid);
	}

	public boolean add(Group group)
	{
		if (group != null && group.getID() > 0)
			return groups.add(group.getID(), group);

		return false;
	}

	public boolean remove(int gid)
	{
		return groups.removeKey(gid);
	}

	public void clear()
	{
		groups.clear();
	}

	public int size()
	{
		return groups.size();
	}

	@Override
	public Iterator<Group> iterator()
	{
		return groups.iterator();
	}

	@Override
	public String toString()
	{
		return groups.toString();
	}
}

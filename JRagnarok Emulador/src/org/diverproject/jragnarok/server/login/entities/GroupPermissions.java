package org.diverproject.jragnarok.server.login.entities;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

public class GroupPermissions
{
	private Map<String, GroupPermission> permissions;

	public GroupPermissions()
	{
		permissions = new StringSimpleMap<>();
	}

	public boolean add(GroupPermission permission)
	{
		if (permission != null)
			return permissions.add(permission.getName(), permission);

		return false;
	}

	public boolean remove(GroupPermission permission)
	{
		if (permission != null)
			return permissions.removeKey(permission.getName());

		return false;
	}

	public boolean contains(String name)
	{
		return permissions.containsKey(name);
	}

	public boolean contains(GroupPermission permission)
	{
		if (permission != null)
			return contains(permission.getName());

		return false;
	}

	public int size()
	{
		return permissions.size();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		for (GroupPermission permission : permissions)
			description.append(permission.getName());

		return description.toString();
	}
}

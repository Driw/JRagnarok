package org.diverproject.jragnarok.server.common.entities;

import java.util.Iterator;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.Map.MapItem;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

public class GroupPermissions implements Iterable<MapItem<String, Integer>>
{
	public static final String ENABLED_ALL = "all_permissions";

	private boolean enabledAll;
	private Map<String, Integer> permissions;

	public GroupPermissions()
	{
		permissions = new StringSimpleMap<>();
	}

	public void set(String permission, int bitmask)
	{
		if (permission != null)
		{
			if (permission.equals(ENABLED_ALL))
				enabledAll = true;

			else
			{
				permissions.removeKey(permission);
				permissions.add(permission, bitmask);
			}
		}
	}

	public void remove(String permission)
	{
		permissions.removeKey(permission);
	}

	public boolean contains(String permission)
	{
		return permissions.containsKey(permission);
	}

	public boolean is(String permission)
	{
		return is(permission, 1);
	}

	public boolean is(String permission, int bitmask)
	{
		if (enabledAll)
			return true;

		Integer permissionMask = permissions.get(permission);

		return permissionMask != null && permissionMask == bitmask;
	}

	public int size()
	{
		return permissions.size();
	}

	@Override
	public Iterator<MapItem<String, Integer>> iterator()
	{
		return permissions.iteratorItems();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		for (MapItem<String, Integer> permission : permissions.iterateItems())
			description.append(permission.key, permission.value);

		return description.toString();
	}
}

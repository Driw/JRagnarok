package org.diverproject.jragnaork.database.impl;

import static org.diverproject.jragnaork.RagnarokAPI.MAX_MAPNAME_LENGTH;
import static org.diverproject.jragnaork.RagnarokAPI.MIN_MAPNAME_LENGTH;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnaork.database.IndexableDatabaseItem;
import org.diverproject.util.ObjectDescription;

public class MapIndex implements IndexableDatabaseItem
{
	private int id;
	private String mapName;

	@Override
	public int getID()
	{
		return id;
	}

	@Override
	public void setID(int id)
	{
		if (id >= 1)
			this.id = id;
	}

	public String getMapName()
	{
		return mapName;
	}

	public void setMapName(String mapName)
	{
		if (mapName != null && interval(mapName.length(), MIN_MAPNAME_LENGTH, MAX_MAPNAME_LENGTH))
			this.mapName = mapName;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("mapName", mapName);

		return description.toString();
	}
}

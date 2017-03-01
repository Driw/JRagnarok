package org.diverproject.jragnaork.database.impl;

import static org.diverproject.jragnaork.JRagnarokConstants.MAX_MAP_NAME_LENGTH;
import static org.diverproject.jragnaork.JRagnarokConstants.MIN_MAP_NAME_LENGTH;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnaork.database.IndexableDatabaseItem;
import org.diverproject.util.ObjectDescription;

public class MapIndex implements IndexableDatabaseItem
{
	private short mapID;
	private String mapName;

	@Override
	public int getID()
	{
		return mapID;
	}

	@Override
	public void setID(int id)
	{
		if (id >= 1)
			this.mapID = s(id);
	}

	public short getMapID()
	{
		return mapID;
	}

	public void setMapID(short mapID)
	{
		this.mapID = mapID;
	}

	public String getMapName()
	{
		return mapName;
	}

	public void setMapName(String mapName)
	{
		if (mapName != null && interval(mapName.length(), MIN_MAP_NAME_LENGTH, MAX_MAP_NAME_LENGTH))
			this.mapName = mapName;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("mapID", mapID);
		description.append("mapName", mapName);

		return description.toString();
	}
}

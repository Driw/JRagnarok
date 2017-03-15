package org.diverproject.jragnarok.database;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_INDEX;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.database.impl.MapIndex;

public class MapIndexes extends IndexableDatabase<MapIndex>
{
	public MapIndexes()
	{
		super(MapIndex.class, "MapIndexDB", MAX_MAP_INDEX);
	}

	public MapIndex get(int index)
	{
		if (interval(index, 1, items.length))
			return items[index - 1];

		return null;
	}

	public int getMapID(String mapname)
	{
		for (MapIndex map : items)
			if (map != null && map.getMapName().equals(mapname))
				return map.getID();

		return 0;
	}

	public String getMapName(int mapid)
	{
		if (interval(mapid, 1, length()))
			if (items[mapid - 1] != null)
				return items[mapid - 1].getMapName();

		return null;
	}

	public boolean contains(String mapname)
	{
		for (int i = 0, f = 0; i < length() && f < size(); i++)
			if (items[i] != null)
			{
				f++;

				if (items[i].getMapName().equals(mapname))
					return true;
			}

		return false;
	}
}

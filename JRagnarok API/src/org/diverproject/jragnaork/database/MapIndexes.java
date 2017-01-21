package org.diverproject.jragnaork.database;

import static org.diverproject.jragnaork.RagnarokAPI.MAX_MAPINDEX;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnaork.database.impl.MapIndex;

public class MapIndexes extends IndexableDatabase<MapIndex>
{
	public MapIndexes()
	{
		super("MapIndexDB", MAX_MAPINDEX);
	}

	public int get(String mapname)
	{
		for (MapIndex map : items)
			if (map != null && map.getMapName().equals(mapname))
				return map.getID();

		return 0;
	}

	public String get(int mapid)
	{
		if (interval(mapid, 1, length()))
			if (items[mapid] != null)
				return items[mapid].getMapName();

		return null;
	}
}

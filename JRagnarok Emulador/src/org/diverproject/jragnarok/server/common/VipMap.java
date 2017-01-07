package org.diverproject.jragnarok.server.common;

import java.util.Iterator;

import org.diverproject.jragnarok.server.common.entities.Vip;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

public class VipMap implements Iterable<Vip>
{
	private Map<Integer, Vip> vips;

	public VipMap()
	{
		vips = new IntegerLittleMap<>();
	}

	public Vip get(int vid)
	{
		return vips.get(vid);
	}

	public boolean add(Vip vip)
	{
		if (vip != null && vip.getID() > 0)
			return vips.add(vip.getID(), vip);

		return false;
	}

	public boolean remove(int vid)
	{
		return vips.removeKey(vid);
	}

	public void clear()
	{
		vips.clear();
	}

	public int size()
	{
		return vips.size();
	}

	@Override
	public Iterator<Vip> iterator()
	{
		return vips.iterator();
	}

	@Override
	public String toString()
	{
		return vips.toString();
	}
}

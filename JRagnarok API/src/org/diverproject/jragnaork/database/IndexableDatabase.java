package org.diverproject.jragnaork.database;

import static org.diverproject.util.Util.format;
import static org.diverproject.util.lang.IntUtil.interval;

import java.util.Iterator;

import org.diverproject.util.ObjectDescription;

public abstract class IndexableDatabase<I extends IndexableDatabaseItem> extends AbstractDatabase<I>
{
	private int size;
	protected I items[];

	@SuppressWarnings("unchecked")
	public IndexableDatabase(String name, int max)
	{
		super(name);

		items = (I[]) new IndexableDatabaseItem[max];
	}

	@Override
	public void clear()
	{
		for (int i = 0; i < items.length; i++)
			items[i] = null;

		size = 0;
	}

	public boolean insert(I item)
	{
		if (interval(item.getID(), 1, items.length))
			if (items[item.getID()] == null)
			{
				size++;
				items[item.getID()] = item;
				return true;
			}

		return false;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public int length()
	{
		return items.length;
	}

	@Override
	public boolean contains(I item)
	{
		return item != null && contains(item.getID());
	}

	public boolean contains(int id)
	{
		return interval(id, 1, items.length) ? items[id - 1] != null : false;
	}

	@Override
	public Iterator<I> iterator()
	{
		return new Iterator<I>()
		{
			private int iterate;

			@Override
			public boolean hasNext()
			{
				return iterate < items.length;
			}

			@Override
			public I next()
			{
				return items[iterate++];
			}

			@Override
			public String toString()
			{
				ObjectDescription description = new ObjectDescription(getClass());

				description.append("name", getName());
				description.append("size", format("%d/%d", size(), length()));

				return description.toString();
			}
		};
	}
}

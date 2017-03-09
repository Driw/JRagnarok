package org.diverproject.jragnarok.database;

import static org.diverproject.util.Util.format;

import java.util.Iterator;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.ArrayUtil;

public class SequencialDatabase <I extends IndexableDatabaseItem> extends AbstractDatabase<I>
{
	private int size;
	private I items[];

	@SuppressWarnings("unchecked")
	public SequencialDatabase(String name, int length)
	{
		super(name);

		this.items = (I[]) new IndexableDatabaseItem[length];
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
		if (space())
			for (int i = 0; i < items.length; i++)
			{
				if (items[i].getID() == item.getID())
					return false;
				else if (items[i].getID() > item.getID() && ArrayUtil.moveRight(items, i))
				{
					size++;
					return false;
				}
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
		if (item != null)
			for (I i : items)
				if (i.getID() == item.getID())
					return true;

		return false;
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

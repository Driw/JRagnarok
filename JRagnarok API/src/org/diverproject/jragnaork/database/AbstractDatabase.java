package org.diverproject.jragnaork.database;

import static org.diverproject.util.Util.format;

import org.diverproject.util.ObjectDescription;

public abstract class AbstractDatabase<I> implements GenericDatabase<I>
{
	private String name;

	public AbstractDatabase(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public boolean space()
	{
		return length() != size();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("name", name);
		description.append("space", format("%d/%d", size(), length()));

		return description.toString();
	}
}

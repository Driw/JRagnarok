package org.diverproject.jragnaork.configuration;

import org.diverproject.util.ObjectDescription;

public abstract class Config<T>
{
	private String name;

	public Config(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public abstract T getValue();

	public abstract void setValue(T value);

	public abstract void setObject(Object object);

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("name", name);
		description.append("value", getValue());

		return description.toString();
	}
}

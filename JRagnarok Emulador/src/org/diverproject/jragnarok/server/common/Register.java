package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

public class Register<E>
{
	private String key;
	private boolean updatable;
	private E value;

	public Register(String key)
	{
		this.key = key;
	}

	public String getKey()
	{
		return key;
	}

	public E getValue()
	{
		return value;
	}

	public void setValue(E element)
	{
		if (this.value == null || !this.value.equals(element))
		{
			this.value = element;
			this.updatable = true;
		}
	}

	public boolean isUpdatable()
	{
		return updatable;
	}

	public void setUpdatable(boolean updatable)
	{
		this.updatable = updatable;
	}

	public void toString(ObjectDescription description)
	{
		description.append("key", value);

		if (updatable)
			description.append("updatable");
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());
		toString(description);

		return description.toString();
	}
}

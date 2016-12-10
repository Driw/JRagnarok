package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

public class GlobalAccountReg
{
	private int index;
	private String key;
	private int operation;

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public int getOperation()
	{
		return operation;
	}

	public void setOperation(int operation)
	{
		this.operation = operation;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("index", index);
		description.append("key", key);
		description.append("operation", operation);

		return description.toString();
	}
}

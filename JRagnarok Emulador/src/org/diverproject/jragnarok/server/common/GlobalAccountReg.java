package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

public class GlobalAccountReg
{
	private String key;
	private int operation;
	private Object value;

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

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("key", key);
		description.append("operation", operation);

		return description.toString();
	}
}

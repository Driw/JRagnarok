package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

public class GlobalRegisterOperation<E>
{
	private int operation;
	private GlobalRegister<E> register;

	public int getOperation()
	{
		return operation;
	}

	public void setOperation(int operation)
	{
		this.operation = operation;
	}

	public GlobalRegister<E> getRegister()
	{
		return register;
	}

	public void setRegister(GlobalRegister<E> register)
	{
		this.register = register;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		if (register != null)
			register.toString(description);

		description.append("operation", operation);

		return description.toString();
	}
}

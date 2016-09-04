package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.util.ObjectDescription;

public class LoginSeed
{
	private long first;
	private long second;

	public long getFirst()
	{
		return first;
	}

	public void genFirst()
	{
		// TODO random
	}

	public long getSecond()
	{
		return second;
	}

	public void genSecond()
	{
		// TODO random
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("first", first);
		description.append("second", second);

		return description.toString();
	}
}

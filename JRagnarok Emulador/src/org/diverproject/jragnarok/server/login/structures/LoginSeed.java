package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokUtil.random;

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
		first = random() + 1;
	}

	public long getSecond()
	{
		return second;
	}

	public void genSecond()
	{
		second = random() + 1;
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

package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokUtil.random;

import org.diverproject.util.ObjectDescription;

public class LoginSeed
{
	private int first;
	private int second;

	public int getFirst()
	{
		return first;
	}

	public void genFirst()
	{
		first = random() + 1;
	}

	public int getSecond()
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

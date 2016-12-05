package org.diverproject.jragnarok.server.common;

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

	public boolean equals(int firstSeed, int secondSeed)
	{
		return first == firstSeed && second == secondSeed;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof LoginSeed)
		{
			LoginSeed seed = (LoginSeed) object;

			return	seed.first == first &&
					seed.second == second;
		}

		return false;
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

package org.diverproject.jragnarok;

public class JRagnarokUtil
{
	public static void sleep(long mileseconds)
	{
		try {
			Thread.sleep(mileseconds);
		} catch (InterruptedException e) {
		}
	}

	public static String nameOf(Object object)
	{
		if (object == null)
			return "null";

		return object.getClass().getSimpleName();
	}
}

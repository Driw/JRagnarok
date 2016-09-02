package org.diverproject.jragnarok;

import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.setUpSource;

import java.util.Locale;

import org.diverproject.util.SizeUtil;
import org.diverproject.util.SystemUtil;

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

	public static String format(String format, Object... args)
	{
		return String.format(format, args);
	}

	public static String time(long ms)
	{
		if (ms < 1000)
			return String.format(Locale.US, "%dms", ms);

		if (ms < 60000)
			return String.format(Locale.US, "%.2fms", (float) ms/1000);

		if (ms < 3600000)
			return String.format(Locale.US, "%dm%.2fms", (int) ms/60000, (int) ms/1000);

		if (ms < 86400000)
			return String.format(Locale.US, "%dh%dm%ds", (int) ms/3600000, (int) ms/60000, (int) ms/1000);

		return String.format(Locale.US, "%d%dh%dm", (int) ms/86400000, (int) ms/3600000, (int) ms/60000);
	}

	public static void free()
	{
		long totalFreeMemory = SystemUtil.getTotalFreeMemory();

		System.gc();

		long newTotalFreeMemory = SystemUtil.getFreeMemory();
		long freeMemory = newTotalFreeMemory - totalFreeMemory;

		setUpSource(1);

		log("%s liberado pelo GC.\n", SizeUtil.toString(freeMemory));
	}
}

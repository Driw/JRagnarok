package org.diverproject.jragnarok;

import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.setUpSource;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.util.SizeUtil;
import org.diverproject.util.SystemUtil;

public class JRagnarokUtil
{
	private JRagnarokUtil()
	{
		
	}

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

	public static String strcap(String string, int length)
	{
		if (string == null)
			return "";

		if (string.length() > length)
			return string.substring(0, length);

		return string;
	}

	public static String md5Encrypt(String string)
	{
		try {

			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte encrypted[] = messageDigest.digest(string.getBytes());
			String md5String =  new String(encrypted);

			return md5String;

		} catch (NoSuchAlgorithmException e) {

			setUpSource(1);
			logExeception(e);

			throw new RagnarokRuntimeException(e.getMessage());
		}
	}

	public static String binToHex(String string, int count)
	{
		String output = "";
		String toHex = "0123456789abcdef";

		for (int i = 0; i < count; i++)
		{
			output += toHex.charAt(string.charAt(i) & 0xF0 >> 4);
			output += toHex.charAt(string.charAt(i) & 0x0F >> 0);
		}

		return output;
	}
}

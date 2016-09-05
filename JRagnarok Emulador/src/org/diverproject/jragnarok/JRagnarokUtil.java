package org.diverproject.jragnarok;

import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.setUpSource;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnaork.messages.Messages;
import org.diverproject.jragnarok.server.FileDecriptor;
import org.diverproject.util.SizeUtil;
import org.diverproject.util.SystemUtil;
import org.diverproject.util.collection.List;

public class JRagnarokUtil
{
	private JRagnarokUtil()
	{
		
	}

	private static final Random random = new Random();

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

	public static String md5Salt(int length)
	{
		byte bytes[] = new byte[length];

		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) (1 + random() % 255);

		return new String(bytes);
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

	@SuppressWarnings("rawtypes")
	public static int indexOn(List list, Object target)
	{
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).equals(target))
				return i + 1;

		return 0;
	}

	public static void skip(FileDecriptor fd, boolean input, int bytes)
	{
		if (input)
			fd.newInput("SkipPacket").skipe(bytes);
		else
			fd.newOutput("SkipPacket").skipe(bytes);
	}

	public static int random()
	{
		int i = random.nextInt();

		return i > 0 ? i : i * -1;
	}

	public static String loginMessage(int number)
	{
		return Messages.getInstance().getLoginMessages().get(number);
	}

	public static String charMessage(int number)
	{
		return Messages.getInstance().getCharMessages().get(number);
	}

	public static String mapMessage(int number)
	{
		return Messages.getInstance().getMapMessages().get(number);
	}

	public static int dateToVersion(int date)
	{
			 if(date < 20040906) return 5;
		else if(date < 20040920) return 10;
		else if(date < 20041005) return 11;
		else if(date < 20041025) return 12;
		else if(date < 20041129) return 13;
		else if(date < 20050110) return 14;
		else if(date < 20050509) return 15;
		else if(date < 20050628) return 16;
		else if(date < 20050718) return 17;
		else if(date < 20050719) return 18;
		else if(date < 20060327) return 19;
		else if(date < 20070108) return 20;
		else if(date < 20070212) return 21;
		else if(date < 20080910) return 22;
		else if(date < 20080827) return 23;
		else if(date < 20080910) return 24;
		else if(date < 20101124) return 25;
		else if(date < 20111005) return 26;
		else if(date < 20111102) return 27;
		else if(date < 20120307) return 28;
		else if(date < 20120410) return 29;
		else if(date < 20120418) return 30;
		else if(date < 20120618) return 31;
		else if(date < 20120702) return 32;
		else if(date < 20130320) return 33;
		else if(date < 20130515) return 34;
		else if(date < 20130522) return 35;
		else if(date < 20130529) return 36;
		else if(date < 20130605) return 37;
		else if(date < 20130612) return 38;
		else if(date < 20130618) return 39;
		else if(date < 20130626) return 40;
		else if(date < 20130703) return 41;
		else if(date < 20130710) return 42;
		else if(date < 20130717) return 43;
		else if(date < 20130807) return 44;
		else if(date < 20131223) return 45;
		else if(date < 20140212) return 46;
		else if(date < 20140613) return 47;
		else if(date < 20141016) return 48;
		else if(date < 20141022) return 50;
		else if(date < 20150513) return 51;
		else if(date < 20150916) return 52;
		else if(date < 20151001) return 53;
		else if(date < 20151104) return 54;
		else if(date >= 20151104) return 55;

		else return 30;
	}
}

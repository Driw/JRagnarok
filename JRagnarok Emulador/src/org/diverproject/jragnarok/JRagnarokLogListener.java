package org.diverproject.jragnarok;

import org.diverproject.log.Log;
import org.diverproject.log.LogListener;

public class JRagnarokLogListener implements LogListener
{
	private static final LogListener INSTANCE = new JRagnarokLogListener();

	@Override
	public void onMessage(Log log)
	{
		System.out.print(log.toString());
	}

	public static LogListener getInstance()
	{
		return INSTANCE;
	}
}

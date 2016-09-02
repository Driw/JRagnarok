package org.diverproject.jragnarok.server;

import org.diverproject.util.BitWise;

public class Timer
{
	public static final int TIMER_ONCE_AUTODEL = 0x01;
	public static final int TIMER_INTERVAL = 0x02;
	public static final int TIMER_REMOVE = 0x10;

	private static final String TIMER_STRINGS[] = new String[]
	{ "ONCE_AUTODEL", "INTERVAL", "0x04", "0x08", "REMOVE" };

	private static int autoIncrement;

	private int id;
	private long tick;
	private int interval;
	private int data;
	private BitWise type;
	private TimerListener listener;

	Timer()
	{
		id = ++autoIncrement;
		type = new BitWise(TIMER_STRINGS);
	}

	public int getID()
	{
		return id;
	}

	void setID(int id)
	{
		this.id = id;
	}

	public long getTick()
	{
		return tick;
	}

	void setTick(long tick)
	{
		this.tick = tick;
	}

	int getInterval()
	{
		return interval;
	}

	void setInterval(int interval)
	{
		this.interval = interval;
	}

	public int getData()
	{
		return data;
	}

	void setData(int data)
	{
		this.data = data;
	}

	BitWise getType()
	{
		return type;
	}

	TimerListener getListener()
	{
		return listener;
	}

	void setListener(TimerListener listener)
	{
		this.listener = listener;
	}
}

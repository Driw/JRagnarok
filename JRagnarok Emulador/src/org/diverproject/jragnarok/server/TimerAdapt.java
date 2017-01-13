package org.diverproject.jragnarok.server;

public abstract class TimerAdapt implements TimerListener
{
	@Override
	public String toString()
	{
		return getName();
	}
}

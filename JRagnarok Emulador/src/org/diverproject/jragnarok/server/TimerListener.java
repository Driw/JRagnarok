package org.diverproject.jragnarok.server;

public interface TimerListener
{
	void onCall(Timer timer, long tick);
}

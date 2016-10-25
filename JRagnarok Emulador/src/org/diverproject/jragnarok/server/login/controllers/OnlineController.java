package org.diverproject.jragnarok.server.login.controllers;

import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.entities.OnlineLogin;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

public class OnlineController
{
	private static final Map<Integer, OnlineLogin> onlines;

	static
	{
		onlines = new IntegerLittleMap<>();
	}

	public OnlineLogin get(int id)
	{
		return onlines.get(id);
	}

	public void add(OnlineLogin online)
	{
		if (online.getWaitingDisconnect() != null)
		{
			TimerSystem.getInstance().getTimers().delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		onlines.add(online.getAccountID(), online);
	}

	public void remove(OnlineLogin online)
	{
		if (online.getWaitingDisconnect() != null)
		{
			TimerSystem.getInstance().getTimers().delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		onlines.remove(online);
	}
}

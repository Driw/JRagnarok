package org.diverproject.jragnarok.server.login.controllers;

import org.diverproject.jragnarok.server.login.entities.OnlineLogin;
import org.diverproject.jragnarok.server.login.services.LoginServerService;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

public class OnlineController
{
	private LoginServerService service;
	private final Map<Integer, OnlineLogin> onlines;

	public OnlineController(LoginServerService service)
	{
		this.service = service;
		this.onlines = new IntegerLittleMap<>();
	}

	public OnlineLogin get(int id)
	{
		return onlines.get(id);
	}

	public void add(OnlineLogin online)
	{
		if (online.getWaitingDisconnect() != null)
		{
			service.getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		onlines.add(online.getAccountID(), online);
	}

	public void remove(OnlineLogin online)
	{
		if (online.getWaitingDisconnect() != null)
		{
			service.getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		onlines.remove(online);
	}
}

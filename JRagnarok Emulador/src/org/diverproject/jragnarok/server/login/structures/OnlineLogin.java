package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.jragnarok.server.Timer;

public class OnlineLogin
{
	public static final int OFFLINE = -2;
	public static final int NONE = -1;

	private int accountID;
	private int charServerID;
	private Timer waitingDisconnect;

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public Timer getWaitingDisconnect()
	{
		return waitingDisconnect;
	}

	public void setWaitingDisconnect(Timer waitingDisconnect)
	{
		this.waitingDisconnect = waitingDisconnect;
	}

	public int getCharServerID()
	{
		return charServerID;
	}

	public void setCharServer(int charServerID)
	{
		this.charServerID = charServerID;
	}
}

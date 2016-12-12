package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.util.ObjectDescription;

/**
 * TODO
 *
 * @author Andrew
 */

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

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);
		description.append("charServerID", charServerID);
		description.append("waitingDisconnect", waitingDisconnect.getID());

		return super.toString();
	}
}

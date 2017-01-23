package org.diverproject.jragnarok.server.common;

public enum DisconnectPlayer
{
	DP_KICK_OFFLINE(1),
	DP_KICK_ONLINE(2);

	public final int CODE;

	private DisconnectPlayer(int code)
	{
		CODE = code;
	}
}

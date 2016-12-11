package org.diverproject.jragnarok.server.common;

public enum DisconnectPlayer
{
	KICK_OFFLINE(1),
	KICK_ONLINE(2);

	public final int CODE;

	private DisconnectPlayer(int code)
	{
		CODE = code;
	}
}

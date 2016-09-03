package org.diverproject.jragnarok.server;

public enum CharServerType
{
	UNKNOW(-1),
	NORMAL(0),
	MAINTENANCE(1),
	OVER_EIGHTEEN(2),
	PAYING(3),
	PEER_TO_PEER(4);

	private short code;

	CharServerType(int code)
	{
		this.code = (short) code;
	}

	public short getCode()
	{
		return code;
	}

	public static CharServerType parse(int code)
	{
		switch (code)
		{
			case 0: return NORMAL;
			case 1: return MAINTENANCE;
			case 2: return OVER_EIGHTEEN;
			case 3: return PAYING;
			case 4: return PEER_TO_PEER;
		}

		return UNKNOW;
	}
}

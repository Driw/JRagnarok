package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokUtil.s;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum CharServerType
{
	NORMAL(0),
	MAINTANCE(1),
	OVER(2),
	PLAYING(3),
	P2P(4);

	public final short CODE;

	private CharServerType(int code)
	{
		CODE = s(code);
	}

	public static CharServerType parse(int code)
	{
		switch (code)
		{
			case 0: return NORMAL;
			case 1: return MAINTANCE;
			case 2: return OVER;
			case 3: return PLAYING;
			case 4: return P2P;
		}

		throw new RagnarokRuntimeException("code não é CharServerType");
	}
}

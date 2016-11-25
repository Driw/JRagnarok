package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokUtil.s;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum CharServerType
{
	NORMAL(0),
	MAINTANCE(1),
	OVER_AGE(2),
	PAY_TO_PLAY(3),
	PREE_TO_PLAY(4);

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
			case 2: return OVER_AGE;
			case 3: return PAY_TO_PLAY;
			case 4: return PREE_TO_PLAY;
		}

		throw new RagnarokRuntimeException("code não é CharServerType");
	}
}

package org.diverproject.jragnarok.packets.common;

import static org.diverproject.jragnarok.JRagnarokUtil.b;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum BanNotification
{
	CHANGE_OF_STATUS(0),
	BAN(1);

	public final byte CODE;

	private BanNotification(int code)
	{
		CODE = b(code);
	}

	public static BanNotification parse(byte code)
	{
		switch (code)
		{
			case 0: return CHANGE_OF_STATUS;
			case 1: return BAN;
		}

		throw new RagnarokRuntimeException("BanNotification#%d não encontrado", code);
	}
}
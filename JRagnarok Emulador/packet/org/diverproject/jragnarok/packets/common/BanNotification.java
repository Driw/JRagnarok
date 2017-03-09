package org.diverproject.jragnarok.packets.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.RagnarokRuntimeException;

public enum BanNotification
{
	BN_CHANGE_OF_STATUS(0),
	BN_BANNED(1);

	public final byte CODE;

	private BanNotification(int code)
	{
		CODE = b(code);
	}

	public static BanNotification parse(byte code)
	{
		switch (code)
		{
			case 0: return BN_CHANGE_OF_STATUS;
			case 1: return BN_BANNED;
		}

		throw new RagnarokRuntimeException("BanNotification#%d não encontrado", code);
	}
}
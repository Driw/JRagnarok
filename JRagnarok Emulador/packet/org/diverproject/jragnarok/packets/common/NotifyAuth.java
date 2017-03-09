package org.diverproject.jragnarok.packets.common;

import org.diverproject.jragnarok.RagnarokRuntimeException;

public enum NotifyAuth
{
	NA_SERVER_CLOSED(1),
	NA_ALREADY_ONLINE(2),
	NA_RECOGNIZES_LAST_LOGIN(8);

	public final byte CODE;

	private NotifyAuth(int code)
	{
		CODE = (byte) code;
	}

	public static NotifyAuth parse(int code)
	{
		for (NotifyAuth result : values())
			if (result.CODE == code)
				return result;

		throw new RagnarokRuntimeException("NotifyAuthResult#%d não encontrado", code);
	}
}

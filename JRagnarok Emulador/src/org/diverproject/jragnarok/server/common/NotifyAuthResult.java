package org.diverproject.jragnarok.server.common;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum NotifyAuthResult
{
	SERVER_CLOSED(1),
	RECOGNIZES_LAST_LOGIN(8),;

	public final byte CODE;

	private NotifyAuthResult(int code)
	{
		CODE = (byte) code;
	}

	public static NotifyAuthResult parse(int code)
	{
		for (NotifyAuthResult result : values())
			if (result.CODE == code)
				return result;

		throw new RagnarokRuntimeException("%d não é um NotifyAuthResult", code);
	}
}

package org.diverproject.jragnarok.packets.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.RagnarokRuntimeException;

public enum ResultMapServerConnection
{
	RMSC_SUCCESSFUL(0),
	RMSC_FAILURE(1),
	RMSC_FULL(3);

	public final byte CODE;

	private ResultMapServerConnection(int code)
	{
		CODE = b(code);
	}

	public static ResultMapServerConnection parse(byte code)
	{
		for (ResultMapServerConnection result : values())
			if (result.CODE == code)
				return result;

		throw new RagnarokRuntimeException("ResultMapConnection#%d não encontrado", code);
	}
}

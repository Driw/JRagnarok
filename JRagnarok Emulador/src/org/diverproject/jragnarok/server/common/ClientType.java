package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum ClientType
{
	CT_NONE(0),
	CT_DEFAULT(22);

	public final byte CODE;

	ClientType(int code)
	{
		CODE = b(code);
	}

	public static ClientType parse(byte b)
	{
		for (ClientType type : values())
			if (type.CODE == b)
				return type;

		throw new RagnarokRuntimeException("%d não é um ClientType", b);
	}
}

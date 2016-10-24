package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum ClientType
{
	CT_DEFAULT(22);

	public final int CODE;

	ClientType(int code)
	{
		CODE = code;
	}

	public static ClientType parse(byte b)
	{
		for (ClientType type : values())
			if (type.CODE == b)
				return type;

		throw new RagnarokRuntimeException("%d não é um ClientType", b);
	}
}

package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.util.lang.IntUtil;

public enum ClientType
{
	CT_KOREA,
	CT_AMERICA,
	CT_JAPAN,
	CT_CHINA,
	CT_TAIWAN,
	CT_THAI,
	CT_INDONESIA,
	CT_PHILIPPINE,
	CT_MALAYSIA,
	CT_SINGAPORE,
	CT_GERMANY,
	CT_INDIA,
	CT_BRAZIL,
	CT_AUSTRALIA,
	CT_RUSSIA,
	CT_VIETNAM,
	CT_CHILE,
	CT_FRANCE,
	CT_UAE;

	public final int CODE;

	ClientType()
	{
		CODE = ordinal();
	}

	public static ClientType parse(byte b)
	{
		ClientType types[] = ClientType.values();

		if (IntUtil.interval(b, 0, types.length - 1))
			return types[b];

		throw new RagnarokRuntimeException("%d não é um ClientType", b);
	}
}

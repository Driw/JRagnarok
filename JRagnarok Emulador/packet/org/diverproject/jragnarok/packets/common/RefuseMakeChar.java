package org.diverproject.jragnarok.packets.common;

import static org.diverproject.jragnarok.JRagnarokUtil.b;

public enum RefuseMakeChar
{
	NAME_USED(0x00),
	UNDERAGED(0x01),
	SYMBOLS_FORBIDDEN(0x02),
	NO_AVAIABLE_SLOT(0x03),
	CREATION_DENIED(0xFF);

	public final byte CODE;

	private RefuseMakeChar(int code)
	{
		CODE = b(code);
	}
}

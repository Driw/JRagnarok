package org.diverproject.jragnarok.packets.common;

import static org.diverproject.jragnarok.JRagnarokUtil.b;

public enum RefuseMakeChar
{
	RMC_NAME_IN_USE(0x00),
	RMC_UNDERAGED(0x01),
	RMC_SYMBOLS_FORBIDDEN(0x02),
	RMC_UNAVAIABLE_SLOT(0x03),
	RMC_CREATION_DENIED(0xFF);

	public final byte CODE;

	private RefuseMakeChar(int code)
	{
		CODE = b(code);
	}
}

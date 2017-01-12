package org.diverproject.jragnarok.packets.common;

import static org.diverproject.jragnarok.JRagnarokUtil.b;

public enum RefuseDeleteChar
{
	RDC_INCORRET_EMAIL_ADDRESS(0x00),
	RDC_CANNOT_BE_DELETED(0x01),
	RDC_PARTY_OR_GUILD(0x02),
	RDC_DENIED(0x03);

	public final byte CODE;

	private RefuseDeleteChar(int code)
	{
		CODE = b(code);
	}
}

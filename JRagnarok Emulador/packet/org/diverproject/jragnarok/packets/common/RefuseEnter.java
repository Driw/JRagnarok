package org.diverproject.jragnarok.packets.common;

import static org.diverproject.jragnarok.JRagnarokUtil.b;

public enum RefuseEnter
{
	REJECTED_FROM_SERVER(0);

	public final byte CODE;

	private RefuseEnter(int code)
	{
		this.CODE = b(code);
	}
}

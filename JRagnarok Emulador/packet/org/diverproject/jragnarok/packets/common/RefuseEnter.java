package org.diverproject.jragnarok.packets.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.RagnarokRuntimeException;

public enum RefuseEnter
{
	RE_REJECTED_FROM_SERVER(0);

	public final byte CODE;

	private RefuseEnter(int code)
	{
		this.CODE = b(code);
	}

	public static final RefuseEnter parse(byte code)
	{
		switch (code)
		{
			case 0: return RefuseEnter.RE_REJECTED_FROM_SERVER;
		}

		throw new RagnarokRuntimeException("RefuseEnter#%d não encontrado", code);
	}
}

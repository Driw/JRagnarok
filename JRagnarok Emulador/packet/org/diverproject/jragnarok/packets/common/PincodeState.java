package org.diverproject.jragnarok.packets.common;

import static org.diverproject.util.Util.s;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum PincodeState
{
	PS_OK(0),
	PS_ASK(1),
	PS_NOTSET(2),
	PS_EXPIRED(3),
	PS_NEW(4),
	PS_ILLEGAL(5),
	PS_KSSN(6),
	PS_SKIP(7),
	PS_WRONG(8);

	public final short CODE;

	private PincodeState(int code)
	{
		CODE = s(code);
	}

	public static final PincodeState parse(short code)
	{
		switch (code)
		{
			case 0: return PS_OK;
			case 1: return PS_ASK;
			case 2: return PS_NOTSET;
			case 3: return PS_EXPIRED;
			case 4: return PS_NEW;
			case 5: return PS_ILLEGAL;
			case 6: return PS_KSSN;
			case 7: return PS_SKIP;
			case 8: return PS_WRONG;
		}

		throw new RagnarokRuntimeException("PincodeState#%d não encontrado", code);
	}
}
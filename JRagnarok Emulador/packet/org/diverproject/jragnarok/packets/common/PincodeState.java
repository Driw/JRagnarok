package org.diverproject.jragnarok.packets.common;

import static org.diverproject.jragnarok.JRagnarokUtil.s;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum PincodeState
{
	OK(0),
	ASK(1),
	NOTSET(2),
	EXPIRED(3),
	NEW(4),
	ILLEGAL(5),
	KSSN(6),
	SKIP(7),
	WRONG(8);

	public final short CODE;

	private PincodeState(int code)
	{
		CODE = s(code);
	}

	public static final PincodeState parse(short code)
	{
		switch (code)
		{
			case 0: return OK;
			case 1: return ASK;
			case 2: return NOTSET;
			case 3: return EXPIRED;
			case 4: return NEW;
			case 5: return ILLEGAL;
			case 6: return KSSN;
			case 7: return SKIP;
			case 8: return WRONG;
		}

		throw new RagnarokRuntimeException("PincodeState#%d não encontrado", code);
	}
}
package org.diverproject.jragnarok.packets.common;

public enum DeleteCharCancel
{
	DCC_SUCCCESS_CANCEL(0x01),
	DCC_DATABASE_ERROR_CANCEL(0x02);

	public final int CODE;

	private DeleteCharCancel(int code)
	{
		CODE = code;
	}
}

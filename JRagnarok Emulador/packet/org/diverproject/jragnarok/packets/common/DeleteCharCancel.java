package org.diverproject.jragnarok.packets.common;

public enum DeleteCharCancel
{
	SUCCCESS_CANCEL(0x01),
	DATABASE_ERROR_CANCEL(0x02);

	public final int CODE;

	private DeleteCharCancel(int code)
	{
		CODE = code;
	}
}

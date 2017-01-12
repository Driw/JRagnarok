package org.diverproject.jragnarok.packets.common;

public enum DeleteCharReserved
{
	ALREADY_ON_QUEUE(0x00),
	ADDED_TO_QUEUE(0x01),
	CHAR_NOT_FOUND(0x03),
	CHAR_WITH_GUILD(0x04),
	CHAR_WITH_PARTY(0x05);

	public final int CODE;

	private DeleteCharReserved(int code)
	{
		CODE = code;
	}
}

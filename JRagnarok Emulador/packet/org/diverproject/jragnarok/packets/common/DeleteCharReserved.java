package org.diverproject.jragnarok.packets.common;

public enum DeleteCharReserved
{
	DCR_ALREADY_ON_QUEUE(0x00),
	DCR_ADDED_TO_QUEUE(0x01),
	DCR_CHAR_NOT_FOUND(0x03),
	DCR_CHAR_WITH_GUILD(0x04),
	DCR_CHAR_WITH_PARTY(0x05);

	public final int CODE;

	private DeleteCharReserved(int code)
	{
		CODE = code;
	}
}

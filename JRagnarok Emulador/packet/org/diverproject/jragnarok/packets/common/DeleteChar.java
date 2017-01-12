package org.diverproject.jragnarok.packets.common;

public enum DeleteChar
{
	UNKNOW_ERROR(0x00),
	SUCCCESS_DELETE(0x01),
	DUE_SETTINGS(0x02),
	DATABASE_ERROR_DELETE(0x03),
	NOT_YET_POSSIBLE_TIME(0x04),
	BIRTH_DATE(0x05);

	public final int CODE;

	private DeleteChar(int code)
	{
		CODE = code;
	}
}

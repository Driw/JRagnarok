package org.diverproject.jragnarok.packets.common;

public enum DeleteChar
{
	DC_UNKNOW_ERROR(0x00),
	DC_SUCCCESS_DELETE(0x01),
	DC_DUE_SETTINGS(0x02),
	DC_DATABASE_ERROR_DELETE(0x03),
	DC_NOT_YET_POSSIBLE_TIME(0x04),
	DC_BIRTH_DATE(0x05);

	public final int CODE;

	private DeleteChar(int code)
	{
		CODE = code;
	}
}

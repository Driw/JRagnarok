package org.diverproject.jragnarok.packets.common;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum RefuseLogin
{
	OK(-1),
	UNREGISTERED_ID(0),
	INCORRECT_PASSWORD(1),
	EXPIRED(2),
	REJECTED_FROM_SERVER(3),
	BLOCKED_BY_GM(4),
	EXE_LASTED_VERSION(5),
	BANNED_UNTIL(6),
	POPULATED(7),
	NO_MORE_ACCOUNTS_COMPANY(8),
	REFUSE_BAN_BY_DBA(9),
	REFUSE_EMAIL_NOT_CONFIRMED(10),
	REFUSE_BAN_BY_GM(11),
	REFUSE_TEMP_BAN_FOR_DBWORK(12),
	REFUSE_SELF_LOCK(13),
	REFUSE_NOT_PERMITTED_GROUP(14),
	REFUSE_NOT_PERMITTED_GROUP2(15),
	ERASED(99),
	REMAINS_AT(100),
	HACKING_INVESTIGATION(101),
	BUG_RELATED_INVESTIGATION(102),
	CHARACTER_IS_BEING_DELETED(103),
	CHARACTER_IS_BEING_DELETED2(104);

	public final byte CODE;

	private RefuseLogin(int code)
	{
		CODE = (byte) code;
	}

	public static RefuseLogin parse(int code)
	{
		for (RefuseLogin result : values())
			if (result.CODE == code)
				return result;

		throw new RagnarokRuntimeException("%d não é um AuthResult", code);
	}
}

package org.diverproject.jragnarok.packets.common;

import org.diverproject.jragnarok.RagnarokRuntimeException;

public enum RefuseLogin
{
	RL_OK(-1),
	RL_UNREGISTERED_ID(0),
	RL_INCORRECT_PASSWORD(1),
	RL_EXPIRED(2),
	RL_REJECTED_FROM_SERVER(3),
	RL_BLOCKED_BY_GM(4),
	RL_EXE_LASTED_VERSION(5),
	RL_BANNED_UNTIL(6),
	RL_POPULATED(7),
	RL_NO_MORE_ACCOUNTS_COMPANY(8),
	RL_REFUSE_BAN_BY_DBA(9),
	RL_REFUSE_EMAIL_NOT_CONFIRMED(10),
	RL_REFUSE_BAN_BY_GM(11),
	RL_REFUSE_TEMP_BAN_FOR_DBWORK(12),
	RL_REFUSE_SELF_LOCK(13),
	RL_REFUSE_NOT_PERMITTED_GROUP(14),
	RL_REFUSE_NOT_PERMITTED_GROUP2(15),
	RL_ERASED(99),
	RL_REMAINS_AT(100),
	RL_HACKING_INVESTIGATION(101),
	RL_BUG_RELATED_INVESTIGATION(102),
	RL_CHARACTER_IS_BEING_DELETED(103),
	RL_CHARACTER_IS_BEING_DELETED2(104);

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

		throw new RagnarokRuntimeException("RefuseLogin#%d não encontrado", code);
	}
}

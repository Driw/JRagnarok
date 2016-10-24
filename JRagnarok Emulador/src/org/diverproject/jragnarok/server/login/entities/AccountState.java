package org.diverproject.jragnarok.server.login.entities;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum AccountState
{
	NONE(0),
	BLOCKED(4),
	BANNED(6),
	MANUAL_BAN(9),
	EMAIL_NOT_CONFIRMED(10),
	TEMP_BANNED(11),
	TEMP_MANUAL_BAN(12),
	SELF_LOCK(13),
	ERASED(99),
	REMAINS_AT(100),
	HACKING_INVESTIGATION(101),
	BUG_RELATED_INVESTIGATION(102),
	CHARACTER_DELETING(103),
	CHARACTER_DELETING2(104);

	public final int CODE;

	private AccountState(int code)
	{
		CODE = code;
	}

	public static AccountState parse(int state)
	{
		switch (state)
		{
			case 0: return AccountState.NONE;
			case 4: return AccountState.BLOCKED;
			case 6: return AccountState.BANNED;
			case 9: return AccountState.MANUAL_BAN;
			case 10: return AccountState.EMAIL_NOT_CONFIRMED;
			case 11: return AccountState.TEMP_BANNED;
			case 12: return AccountState.TEMP_MANUAL_BAN;
			case 13: return AccountState.SELF_LOCK;
			case 99: return AccountState.ERASED;
			case 100: return AccountState.REMAINS_AT;
			case 101: return AccountState.HACKING_INVESTIGATION;
			case 102: return AccountState.BUG_RELATED_INVESTIGATION;
			case 103: return AccountState.CHARACTER_DELETING;
			case 104: return AccountState.CHARACTER_DELETING2;
		}

		throw new RagnarokRuntimeException("state % não é AccountState");
	}
}

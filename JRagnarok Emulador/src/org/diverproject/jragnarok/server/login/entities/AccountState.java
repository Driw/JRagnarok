package org.diverproject.jragnarok.server.login.entities;

import org.diverproject.jragnaork.RagnarokRuntimeException;

public enum AccountState
{
	EMAIL_CONFIRMATION(0);

	public final int CODE;

	private AccountState(int code)
	{
		CODE = code;
	}

	public static AccountState parse(int state)
	{
		switch (state)
		{
			case 0: return AccountState.EMAIL_CONFIRMATION;
		}

		throw new RagnarokRuntimeException("state % não é AccountState");
	}
}

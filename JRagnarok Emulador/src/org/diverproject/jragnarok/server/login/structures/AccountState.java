package org.diverproject.jragnarok.server.login.structures;

public enum AccountState
{
	EMAIL_CONFIRMATION(0);

	public final int CODE;

	private AccountState(int code)
	{
		CODE = code;
	}
}

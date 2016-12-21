package org.diverproject.jragnarok.server.common;

import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.util.ObjectDescription;

public class LoginAdapt implements Login
{
	private int accountID;

	public LoginAdapt(int accountID)
	{
		this.accountID = accountID;
	}

	@Override
	public int getID()
	{
		return accountID;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);

		return description.toString();
	}
}

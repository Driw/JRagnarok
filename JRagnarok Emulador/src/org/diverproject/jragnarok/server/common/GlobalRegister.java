package org.diverproject.jragnarok.server.common;

import org.diverproject.util.ObjectDescription;

public class GlobalRegister<E> extends Register<E>
{
	private int accountID;

	public GlobalRegister(int accountID, String key)
	{
		super(key);

		this.accountID = accountID;
	}

	public int getAccountID()
	{
		return accountID;
	}

	@Override
	public void toString(ObjectDescription description)
	{
		description.append("accountID", accountID);

		super.toString(description);
	}
}

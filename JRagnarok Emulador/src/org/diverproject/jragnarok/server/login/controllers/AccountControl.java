package org.diverproject.jragnarok.server.login.controllers;

import java.sql.Connection;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

public class AccountControl
{
	private static final Map<String, Account> accounts;

	static
	{
		accounts = new StringSimpleMap<>();
	}

	private AccountDAO dao;

	public AccountControl(Connection connection)
	{
		dao = new AccountDAO(connection);
	}

	public Account get(String username) throws RagnarokException
	{
		Account account = accounts.get(username);

		if (account == null)
		{
			account = dao.select(username);

			if (account != null)
				accounts.add(username, account);
		}

		return account;
	}

	public boolean save(Account account) throws RagnarokException
	{
		return dao.update(account);
	}
}

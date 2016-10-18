package org.diverproject.jragnarok.server.login.controllers;

import java.sql.Connection;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

public class AccountController
{
	private Map<String, Account> accounts;
	private AccountDAO dao;

	public AccountController(Connection connection)
	{
		accounts = new StringSimpleMap<>();
		dao = new AccountDAO(connection);
	}

	public Account get(String username) throws RagnarokException
	{
		Account account = accounts.get(username);

		if (account == null)
		{
			account = dao.select(username);
			accounts.add(username, account);
		}

		return account;
	}
}

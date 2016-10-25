package org.diverproject.jragnarok.server.login.controllers;

import org.diverproject.jragnarok.server.login.entities.AuthNode;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

public class AuthController
{
	private static final Map<Integer, AuthNode> nodes;

	static
	{
		nodes = new IntegerLittleMap<>();
	}

	public AuthNode get(int id)
	{
		return nodes.get(id);
	}

	public boolean add(AuthNode node)
	{
		return nodes.add(node.getAccountID(), node);
	}

	public boolean remove(int accountID)
	{
		return nodes.removeKey(accountID);
	}

	public boolean remove(AuthNode node)
	{
		return nodes.removeKey(node.getAccountID());
	}
}

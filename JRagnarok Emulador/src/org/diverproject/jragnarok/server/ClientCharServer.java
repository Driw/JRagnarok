package org.diverproject.jragnarok.server;

import java.net.Socket;

import org.diverproject.util.ObjectDescription;

public class ClientCharServer extends Client
{
	private String name;
	private short port;
	private short users;
	private CharServerType type;
	private short newValue;

	public ClientCharServer(Socket socket)
	{
		super(socket);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public short getPort()
	{
		return port;
	}

	public void setPort(short port)
	{
		this.port = port;
	}

	public short getUsers()
	{
		return users;
	}

	public void setUsers(short users)
	{
		this.users = users;
	}

	public CharServerType getType()
	{
		return type;
	}

	public void setType(CharServerType type)
	{
		this.type = type;
	}

	public short getNewValue()
	{
		return newValue;
	}

	public void setNewValue(short newValue)
	{
		this.newValue = newValue;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);
	}
}

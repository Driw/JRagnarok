package org.diverproject.jragnarok.server;

import java.net.Socket;

import org.diverproject.util.ObjectDescription;

public class ClientServer extends Client
{
	private String username;
	private String password;
	private short port;
	private short users;

	public ClientServer(Socket socket)
	{
		super(socket);
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

	@Override
	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("port", port);
		description.append("users", users);
	}
}

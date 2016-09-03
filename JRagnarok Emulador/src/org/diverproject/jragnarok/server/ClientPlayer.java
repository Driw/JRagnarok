package org.diverproject.jragnarok.server;

import java.net.Socket;

public class ClientPlayer extends Client
{
	private String username;
	private String password;

	public ClientPlayer(Socket socket)
	{
		super(socket);
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
}

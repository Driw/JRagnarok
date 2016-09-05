package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.jragnarok.server.FileDecriptor;
import org.diverproject.jragnarok.server.InternetProtocol;

public class ClientCharServer
{
	private FileDecriptor fileDecriptor;
	private String name;
	private InternetProtocol serverIP;
	private short port;
	private short users;
	private CharServerType type;
	private short newValue;

	public ClientCharServer(FileDecriptor fileDecriptor)
	{
		this.fileDecriptor = fileDecriptor;
	}

	public FileDecriptor getFileDecriptor()
	{
		return fileDecriptor;
	}

	public void setFileDecriptor(FileDecriptor fileDecriptor)
	{
		this.fileDecriptor = fileDecriptor;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public InternetProtocol getServerIP()
	{
		return serverIP;
	}

	public void setServerIP(InternetProtocol serverIP)
	{
		this.serverIP = serverIP;
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
	public String toString()
	{
		return super.toString();
	}
}

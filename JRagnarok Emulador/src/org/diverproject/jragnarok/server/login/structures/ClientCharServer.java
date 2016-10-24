package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;

public class ClientCharServer
{
	private FileDescriptor fd;
	private String name;
	private InternetProtocol ip;
	private short port;
	private short users;
	private CharServerType type;
	private short newValue;

	public ClientCharServer(FileDescriptor fileDecriptor)
	{
		this.fd = fileDecriptor;
	}

	public FileDescriptor getFileDecriptor()
	{
		return fd;
	}

	public void setFileDecriptor(FileDescriptor fileDecriptor)
	{
		this.fd = fileDecriptor;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public InternetProtocol getIP()
	{
		return ip;
	}

	public void setIP(InternetProtocol serverIP)
	{
		this.ip = serverIP;
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

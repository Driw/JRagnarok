package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.CharServerType;
import org.diverproject.util.ObjectDescription;

public class ClientCharServer
{
	private FileDescriptor fd;
	private String name;
	private InternetProtocol ip;
	private short port;
	private short users;
	private CharServerType type;
	private short newDisplay;

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
		return newDisplay;
	}

	public void setNewDisplay(short newDisplay)
	{
		this.newDisplay = newDisplay;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("serverIP", ip);
		description.append("serverPort", port);
		description.append("serverName", name);
		description.append("serverType", type);
		description.append("newDisplay", newDisplay);

		return description.toString();
	}
}

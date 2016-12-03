package org.diverproject.jragnarok.server.character.structures;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_PER_SERVER;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.ObjectDescription;

public class ClientMapServer
{
	private FileDescriptor fd;
	private InternetProtocol ip;
	private short port;
	private short users;
	private short map[];

	public ClientMapServer(FileDescriptor fileDecriptor)
	{
		this.fd = fileDecriptor;
		this.map = new short[MAX_MAP_PER_SERVER];
	}

	public FileDescriptor getFileDecriptor()
	{
		return fd;
	}

	public void setFileDecriptor(FileDescriptor fileDecriptor)
	{
		this.fd = fileDecriptor;
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

	public short[] getMaps()
	{
		return map;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("fd", fd.getID());
		description.append("ip", ip);
		description.append("port", port);
		description.append("online", users);

		return description.toString();
	}
}

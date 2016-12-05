package org.diverproject.jragnarok.server.map;

import java.net.Socket;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.map.structures.MapSessionData;

public class MFileDescriptor extends FileDescriptor
{
	private MapSessionData session;

	public MFileDescriptor(Socket socket)
	{
		super(socket);

		session = new MapSessionData();
	}

	@Override
	public MapSessionData getSessionData()
	{
		return session;
	}
}

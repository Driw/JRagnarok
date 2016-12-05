package org.diverproject.jragnarok.server.character;

import java.net.Socket;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;

public class CFileDescriptor extends FileDescriptor
{
	private CharSessionData session;

	public CFileDescriptor(Socket socket)
	{
		super(socket);

		session = new CharSessionData();
	}

	@Override
	public CharSessionData getSessionData()
	{
		return session;
	}
}
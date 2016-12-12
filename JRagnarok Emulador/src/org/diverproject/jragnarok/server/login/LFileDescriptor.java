package org.diverproject.jragnarok.server.login;

import java.net.Socket;

import org.diverproject.jragnarok.server.FileDescriptor;

public class LFileDescriptor extends FileDescriptor
{
	private LoginSessionData session;

	public LFileDescriptor(Socket socket)
	{
		super(socket);

		session = new LoginSessionData();
	}

	@Override
	public LoginSessionData getSessionData()
	{
		return session;
	}
}

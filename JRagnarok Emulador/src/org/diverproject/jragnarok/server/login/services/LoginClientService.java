package org.diverproject.jragnarok.server.login.services;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.ClientPlayer;
import org.diverproject.jragnarok.server.SocketParse;
import org.diverproject.jragnarok.server.login.LoginServer;

public class LoginClientService extends LoginServerService
{
	public LoginClientService(LoginServer server)
	{
		super(server);
	}

	public SocketParse parse = new SocketParse()
	{
		@Override
		public void parse(ClientPlayer player) throws RagnarokException
		{
			
		}
	};
}

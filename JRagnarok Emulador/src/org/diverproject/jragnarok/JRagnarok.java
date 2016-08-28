package org.diverproject.jragnarok;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.character.CharServer;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.map.MapServer;

public class JRagnarok
{
	public static void main(String[] args) throws RagnarokException, InterruptedException
	{
		Server loginServer = LoginServer.getInstance();
		loginServer.create();
		loginServer.run();

		Server charServer = CharServer.getInstance();
		charServer.create();
		charServer.run();

		Server mapServer = MapServer.getInstance();
		mapServer.create();
		mapServer.run();
	}
}

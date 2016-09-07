package org.diverproject.jragnarok;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerState;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.character.CharServer;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.map.MapServer;
import org.diverproject.log.LogPreferences;
import org.diverproject.log.LogSystem;

public class JRagnarok
{
	public static void main(String[] args) throws RagnarokException, InterruptedException
	{
		LogPreferences.setUseAll();
		LogPreferences.setFile("log");
		LogSystem.initialize();
		LogSystem.addListener(JRagnarokLogListener.getInstance());

		TimerSystem timer = TimerSystem.getInstance();
		timer.init();

		Server loginServer = LoginServer.getInstance();
		loginServer.create();
		loginServer.run();

		Server charServer = CharServer.getInstance();
		charServer.create();
		charServer.run();

		Server mapServer = MapServer.getInstance();
		mapServer.create();
		mapServer.run();

		while (!loginServer.isState(ServerState.DESTROYED) &&
				!charServer.isState(ServerState.DESTROYED) &&
				!mapServer.isState(ServerState.DESTROYED))
		{
			long next = timer.update(timer.tick());
			FileDescriptor.update(next);
		}
	}
}

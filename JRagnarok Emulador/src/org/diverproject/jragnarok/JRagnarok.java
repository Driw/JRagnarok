package org.diverproject.jragnarok;

import static org.diverproject.jragnarok.server.ServerState.DESTROYED;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.character.CharServer;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.map.MapServer;
import org.diverproject.log.LogPreferences;
import org.diverproject.log.LogSystem;

public class JRagnarok
{
	private static final Server loginServer = LoginServer.getInstance();
	private static final Server charServer = CharServer.getInstance();
	private static final Server mapServer = MapServer.getInstance();

	public static void main(String[] args) throws RagnarokException, InterruptedException
	{
		LogPreferences.setUseAll();
		LogPreferences.setFile("log");
		LogSystem.initialize();
		LogSystem.addListener(JRagnarokLogListener.getInstance());

		TimerSystem timer = TimerSystem.getInstance();
		timer.init();

		loginServer.create();
		charServer.create();
		mapServer.create();

		loginServer.run();
		charServer.run();
		mapServer.run();

		while (isRunning())
			if (isOk())
			{
				int next = timer.update(timer.tick());
				FileDescriptor.update(next);
			}
	}

	private static boolean isOk()
	{
		return	loginServer.isState(RUNNING) && charServer.isState(RUNNING) && mapServer.isState(RUNNING);
	}

	private static boolean isRunning()
	{
		return !(loginServer.isState(DESTROYED) && charServer.isState(DESTROYED) && mapServer.isState(DESTROYED));
	}
}

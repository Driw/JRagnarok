package org.diverproject.jragnarok;

import static org.diverproject.jragnarok.server.ServerState.DESTROYED;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logExeceptionSource;

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

	public static void main(String[] args)
	{
		LogPreferences.setUseAll();
		LogPreferences.setFile("log");
		LogSystem.initialize();
		LogSystem.addListener(JRagnarokLogListener.getInstance());

		TimerSystem timer = TimerSystem.getInstance();
		timer.init();

		if (create())
		{
			if (!run())
				destroy();
			else
				keepRunning();
		}

		log("SISTEMA ENCERRADO!");
	}

	private static boolean create()
	{
		try {

			loginServer.create();
			charServer.create();
			mapServer.create();

			return true;

		} catch (RagnarokException e) {
			logExeceptionSource(e);
			return false;
		}
	}

	private static boolean run()
	{
		try {

			loginServer.run();
			charServer.run();
			mapServer.run();

			return true;

		} catch (RagnarokException e) {
			logExeceptionSource(e);
			return false;
		}
	}

	private static boolean destroy()
	{
		try {

			mapServer.destroy();
			charServer.destroy();
			loginServer.destroy();

			return true;

		} catch (RagnarokException e) {
			logExeceptionSource(e);
			return false;
		}
	}

	private static void keepRunning()
	{
		try {

			TimerSystem timer = TimerSystem.getInstance();

			while (isRunning())
				if (isOk())
				{
					int next = timer.update(timer.tick());
					FileDescriptor.update(next);
				}

		} catch (Exception e) {
			logExeceptionSource(e);
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

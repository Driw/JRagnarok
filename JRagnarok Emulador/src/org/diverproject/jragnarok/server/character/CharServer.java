package org.diverproject.jragnarok.server.character;

import static org.diverproject.util.MessageUtil.die;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;

public class CharServer extends Server implements ServerListener
{
	private static final String HOST = "localhost";
	private static final int PORT = 6121;

	private static final CharServer INSTANCE;

	static
	{
		CharServer instance = null;

		try {
			instance = new CharServer();
		} catch (RagnarokException e) {
			die(e);
		}

		INSTANCE = instance;
	}

	public CharServer() throws RagnarokException
	{
		setListener(this);
	}

	@Override
	protected CharConfig setServerConfig()
	{
		return new CharConfig();
	}

	@Override
	public void onCreate() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreated() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRunning() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStoped() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroyed() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getThreadName()
	{
		return "Servidor de Personagens";
	}

	@Override
	protected int getThreadPriority()
	{
		return Thread.MIN_PRIORITY;
	}

	@Override
	protected String getAddress()
	{
		return HOST;
	}

	@Override
	protected int getPort()
	{
		return PORT;
	}

	public static CharServer getInstance()
	{
		return INSTANCE;
	}
}

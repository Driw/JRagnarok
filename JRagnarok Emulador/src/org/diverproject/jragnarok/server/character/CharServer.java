package org.diverproject.jragnarok.server.character;

import static org.diverproject.util.MessageUtil.die;

import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.Server;
import org.diverproject.jragnarok.ServerListener;

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
		super(PORT);

		setListener(this);
	}

	public static CharServer getInstance()
	{
		return INSTANCE;
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
	protected void dispatchSocket(Socket socket)
	{
		// TODO Auto-generated method stub
		
	}
}

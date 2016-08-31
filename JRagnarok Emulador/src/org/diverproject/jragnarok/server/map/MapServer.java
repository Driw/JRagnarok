package org.diverproject.jragnarok.server.map;

import static org.diverproject.util.MessageUtil.die;

import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;

public class MapServer extends Server implements ServerListener
{
	private static final String HOST = "localhost";
	private static final int PORT = 5121;

	private static final MapServer INSTANCE;

	static
	{
		MapServer instance = null;

		try {
			instance = new MapServer();
		} catch (RagnarokException e) {
			die(e);
		}

		INSTANCE = instance;
	}

	public MapServer() throws RagnarokException
	{
		super(PORT);

		setListener(this);
	}

	public static MapServer getInstance()
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
		return "Servidor de Mapas";
	}

	@Override
	protected int getThreadPriority()
	{
		return Thread.MAX_PRIORITY;
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

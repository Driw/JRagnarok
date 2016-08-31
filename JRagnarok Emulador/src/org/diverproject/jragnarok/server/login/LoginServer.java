package org.diverproject.jragnarok.server.login;

import static org.diverproject.util.MessageUtil.die;

import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;

public class LoginServer extends Server implements ServerListener
{
	private static final String HOST = "localhost";
	private static final int PORT = 6900;

	private static final LoginServer INSTANCE;

	static
	{
		LoginServer instance = null;

		try {
			instance = new LoginServer();
		} catch (RagnarokException e) {
			die(e);
		}

		INSTANCE = instance;
	}

	public LoginServer() throws RagnarokException
	{
		super(PORT);

		setListener(this);
	}

	public static LoginServer getInstance()
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
		return "Servidor de Acesso";
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

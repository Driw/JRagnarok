package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.server.ServerState.CREATE;
import static org.diverproject.jragnarok.server.ServerState.CREATED;
import static org.diverproject.jragnarok.server.ServerState.DESTROY;
import static org.diverproject.jragnarok.server.ServerState.DESTROYED;
import static org.diverproject.jragnarok.server.ServerState.NONE;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.ServerState.STOPED;
import static org.diverproject.jragnarok.server.ServerState.STOPPING;
import static org.diverproject.log.LogSystem.logExeception;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.util.lang.IntUtil;

public abstract class Server
{
	private static final int MIN_PORT = 1001;
	private static final int MAX_PORT = 65535;
	private static final int SOCKET_BACKLOG = 50;

	private int port;
	private Thread thread;
	private ServerState state;
	private ServerSocket serverSocket;
	private ServerListener listener;

	public Server(int port) throws RagnarokException
	{
		if (!IntUtil.interval(port, MIN_PORT, MAX_PORT))
			throw new RagnarokException("porta %d inválida");

		if (listener != null)
			throw new RagnarokException("listener não definido");

		this.port = port;
		this.state = NONE;
	}

	public void setListener(ServerListener listener)
	{
		this.listener = listener;
	}

	protected void changeState(ServerState newState) throws RagnarokException
	{
		if (newState == state)
			return;

		switch (newState)
		{
			case NONE:
				if (state != DESTROYED)
					changeStateException(newState);
				break;

			case CREATE:
				if (state != NONE && newState != DESTROYED)
					changeStateException(newState);
				break;

			case CREATED:
				if (state != CREATE)
					changeStateException(newState);
				break;

			case RUNNING:
				if (state != CREATED)
					changeStateException(newState);
				break;

			case STOPPING:
				if (state != RUNNING)
					changeStateException(newState);
				break;

			case STOPED:
				if (state != STOPPING)
					changeStateException(newState);
				break;

			case DESTROY:
				if (state != STOPED)
					changeStateException(newState);
				break;

			case DESTROYED:
				if (state != DESTROY)
					changeStateException(newState);
				break;

			default:
				throw new RagnarokException("state %s inválido", newState);
		}

		state = newState;
	}

	private void changeStateException(ServerState newState) throws RagnarokException
	{
		throw new RagnarokException("%s não pode ir para %s", newState, state);
	}

	public final void create() throws RagnarokException
	{
		changeState(CREATE);
		{
			try {

				listener.onCreate();
				{
					Server self = this;
					InetAddress address = InetAddress.getByName(getAddress());

					serverSocket = new ServerSocket(port, SOCKET_BACKLOG, address);
					thread = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							while (state == RUNNING)
							{
								try {

									Socket socket = serverSocket.accept();

									dispatchSocket(socket);

								} catch (IOException e) {
									logExeception(e);
								}
							}

							Thread.interrupted();
						}

						@Override
						public String toString()
						{
							return self.toString();
						}
					});
					thread.setName(getThreadName());
					thread.setPriority(getThreadPriority());
					thread.setDaemon(false);
				}
				listener.onCreated();

			} catch (UnknownHostException e) {
				throw new RagnarokException("host '%s' desconhecido", getAddress());
			} catch (IOException e) {
				throw new RagnarokException(e.getMessage());
			}
		}
		changeState(CREATED);
	}

	@SuppressWarnings("deprecation")
	public final void run() throws RagnarokException
	{
		if (thread == null)
			throw new RagnarokException("thread não criada");

		listener.onRunning();
		{
			if (thread.isInterrupted())
				thread.resume();
			else
				thread.start();
		}

		changeState(RUNNING);
	}

	public final void stop() throws RagnarokException
	{
		if (thread == null)
			throw new RagnarokException("thread não encontrada");

		changeState(STOPPING);
		{
			listener.onStop();
			{
				
			}
		}
		listener.onStoped();
		changeState(ServerState.STOPED);
	}

	public final void destroy() throws RagnarokException
	{
		changeState(DESTROY);
		{
			try {

				listener.onDestroy();
				serverSocket.close();

				state = ServerState.DESTROYED;

				listener.onDestroyed();

				thread.interrupt();
				thread = null;

			} catch (IOException e) {
				throw new RagnarokException(e.getMessage());
			}
		}
		changeState(ServerState.DESTROYED);
	}

	protected abstract String getThreadName();

	protected abstract int getThreadPriority();

	protected abstract String getAddress();

	protected abstract void dispatchSocket(Socket socket);
}

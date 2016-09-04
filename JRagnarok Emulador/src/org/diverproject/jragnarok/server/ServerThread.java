package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.free;
import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.jragnarok.JRagnarokUtil.sleep;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.util.ObjectDescription;

public class ServerThread implements Runnable
{
	private boolean started;
	private boolean closed;
	private boolean running;
	private Thread thread;
	private FileDecriptor decriptor;

	public ServerThread(Server server, FileDecriptor fileDecriptor)
	{
		thread = new Thread(this);
		thread.setDaemon(false);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setName(format("%s:%s", nameOf(server), fileDecriptor.getAddressString()));

		this.decriptor = fileDecriptor;
		this.decriptor.setThread(this);
	}

	public boolean isInterrupted()
	{
		return running;
	}

	@Override
	public void run()
	{
		try {

			while (!closed)
			{
				if (running)
				{
					sleep(1000);
					continue;
				}

				if (decriptor.isConnected() && decriptor.getReceiveListener() != null)
					decriptor.getReceiveListener().onCall(decriptor);
			}

		} catch (RagnarokRuntimeException e) {

			logError("algum dado inesperado foi encontrado (ip: %s).\n", decriptor.getAddressString());
			logExeception(e);

		} catch (RagnarokException e) {

			logError("um erro não inesperado aconteceu (ip: %s).\n", decriptor.getAddressString());
			logExeception(e);

		} catch (Exception e) {
			logExeception(e);
		}

		thread.interrupt();
		thread = null;
		decriptor.close();
		decriptor = null;

		free();
	}

	public void start()
	{
		if (started)
			return;

		closed = false;
		running = true;
		started = true;

		thread.start();
	}

	public void stop()
	{
		running = false;
	}

	public void resume()
	{
		running = true;
	}

	public void close()
	{
		closed = true;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append(decriptor.getAddressString());
		description.append("interrupted", running);

		return description.toString();
	}
}

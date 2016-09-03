package org.diverproject.jragnarok.server;

import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.implementation.input.InputPacket;
import org.diverproject.util.stream.implementation.output.OutputPacket;

public abstract class Client
{
	private int id;
	private Socket socket;
	private InternetProtocol ip;
	private Object cache;
	private boolean interrupted;
	private Thread thread;

	Client(Socket socket)
	{
		if (socket == null)
			throw new NullPointerException();

		if (socket.isClosed())
			throw new RagnarokRuntimeException("conexão fechada");

		this.socket = socket;
		this.ip = new InternetProtocol(socket);
	}

	public InputPacket newInputPacket(String name, int length)
	{
		try {
			return new InputPacket(socket, length, name);
		} catch (StreamException e) {
			logError("falha ao criar InputPacket para %s (ip: %s)", name, ip.get());
			throw new RagnarokRuntimeException(e.getMessage());
		}
	}

	public OutputPacket newOutputPacket(String name, int length)
	{
		try {
			return new OutputPacket(socket, length, name);
		} catch (StreamException e) {
			logError("falha ao criar OutputPacket para %s (ip: %s)", name, ip.get());
			throw new RagnarokRuntimeException(e.getMessage());
		}
	}

	public void close()
	{
		try {

			socket.close();
			socket = null;
			cache = null;

		} catch (IOException e) {
			logExeception(e);
		}
	}

	public boolean isConnected()
	{
		return socket != null && socket.isConnected();
	}

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public Object getCache()
	{
		return cache;
	}

	public void setCache(Object cache)
	{
		this.cache = cache;
	}

	public String getIP()
	{
		return ip.get();
	}

	public void interrupted()
	{
		interrupted = true;
	}

	public void resume()
	{
		interrupted = false;
		thread.start();
	}

	public boolean isInterrupted()
	{
		return interrupted;
	}

	public void setThread(Thread thread)
	{
		this.thread = thread;
	}

	public abstract String getUsername();

	protected void toString(ObjectDescription description)
	{
		description.append("ip", ip.get());
		description.append("usingCache", cache != null);
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		toString(description);

		return description.toString();
	}
}

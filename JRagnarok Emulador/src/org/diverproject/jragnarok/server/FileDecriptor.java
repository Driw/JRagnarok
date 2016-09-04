package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.implementation.input.InputPacket;
import org.diverproject.util.stream.implementation.output.OutputPacket;

public class FileDecriptor
{
	public static final int FLAG_EOF = 1;
	public static final int FLAG_SERVER = 2;
	public static final int FLAG_PING = 3;

	private static int AUTO_INCREMENT = 0;

	private int id;
	private int flag;
	private Socket socket;
	private InternetProtocol address;
	private ServerThread thread;
	private boolean interrupted;
	private FileDecriptorListener receiveListener;
	private FileDecriptorListener sendListener;
	private FileDecriptorListener parseListener;
	private Object cache;

	FileDecriptor(Socket socket)
	{
		this.id = ++AUTO_INCREMENT;
		this.socket = socket;
		this.address = new InternetProtocol(socket);
	}

	public int getID()
	{
		return id;
	}

	void setID(int id)
	{
		this.id = id;
	}

	public int getFlag()
	{
		return flag;
	}

	public void setFlag(int flag)
	{
		if (flag >= FLAG_EOF && flag <= FLAG_PING)
			this.flag = flag;
	}

	public int getAddress()
	{
		return address.get();
	}

	public String getAddressString()
	{
		return address.getString();
	}

	public ServerThread getThread()
	{
		return thread;
	}

	void setThread(ServerThread thread)
	{
		this.thread = thread;
	}

	public boolean isInterrupted()
	{
		return interrupted;
	}

	public FileDecriptorListener getReceiveListener()
	{
		return receiveListener;
	}

	public void setReceiveListener(FileDecriptorListener receiveListener)
	{
		this.receiveListener = receiveListener;
	}

	public FileDecriptorListener getSendListener()
	{
		return sendListener;
	}

	public void setSendListener(FileDecriptorListener sendListener)
	{
		this.sendListener = sendListener;
	}

	public FileDecriptorListener getParseListener()
	{
		return parseListener;
	}

	public void setParseListener(FileDecriptorListener parseListener)
	{
		this.parseListener = parseListener;
	}

	public Object getCache()
	{
		return cache;
	}

	public void setCache(Object cache)
	{
		this.cache = cache;
	}

	public InputPacket newInput(String name)
	{
		try {
			return new InputPacket(socket, 0, name);
		} catch (StreamException e ) {
			logError("falha ao criar InputPacket para %s (ip: %s)", name, address.getString());
			throw new RagnarokRuntimeException(e.getMessage());
		}
	}

	public OutputPacket newOutput(String name)
	{
		try {
			return new OutputPacket(socket, 0, name);
		} catch (StreamException e ) {
			logError("falha ao criar OutputPacket para %s (ip: %s)", name, address.getString());
			throw new RagnarokRuntimeException(e.getMessage());
		}
	}

	public void close()
	{
		try {

			socket.close();
			socket = null;

		} catch (IOException e) {
			logExeception(e);
		}
	}

	public boolean isConnected()
	{
		return socket != null && socket.isConnected();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("address", address.getString());

		if (thread != null)
			description.append("interrupted", thread.isInterrupted());

		description.append("receive", receiveListener);
		description.append("send", sendListener);
		description.append("parse", parseListener);

		if (cache != null)
			description.append("cache", nameOf(cache));

		return description.toString();
	}
}

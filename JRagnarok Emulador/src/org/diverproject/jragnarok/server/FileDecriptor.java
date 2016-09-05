package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokConstants.FD_SETSIZE;
import static org.diverproject.jragnarok.JRagnarokUtil.indexOn;
import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.setUpSource;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.LoopList;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.implementation.input.InputPacket;
import org.diverproject.util.stream.implementation.output.OutputPacket;

public class FileDecriptor
{
	public static final int FLAG_EOF = 1;
	public static final int FLAG_SERVER = 2;
	public static final int FLAG_PING = 3;

	private int id;
	private int flag;
	private int timeout;
	private Socket socket;
	private InternetProtocol address;
	private boolean interrupted;
	private FileDecriptorListener receiveListener;
	private FileDecriptorListener sendListener;
	private FileDecriptorListener parseListener;
	private Object cache;

	private FileDecriptor(Socket socket)
	{
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

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public int getAddress()
	{
		return address.get();
	}

	public String getAddressString()
	{
		return address.getString();
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
			SESSIONS.remove(id);
			id = 0;

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

		description.append("receive", receiveListener);
		description.append("send", sendListener);
		description.append("parse", parseListener);

		if (cache != null)
			description.append("cache", nameOf(cache));

		return description.toString();
	}

	private static final List<FileDecriptor> SESSIONS = new LoopList<>(FD_SETSIZE);

	public static FileDecriptor newFileDecriptor(Socket socket)
	{
		FileDecriptor fd = new FileDecriptor(socket);

		if (!SESSIONS.add(fd))
		{
			fd.close();

			return null;
		}

		fd.setID(indexOn(SESSIONS, fd));

		return fd;
	}

	public static void update(long next)
	{
		TimerSystem timer = TimerSystem.getInstance();
		long lastTick = timer.getLastTickCount();

		for (FileDecriptor fd : SESSIONS)
		{
			if (fd.getTimeout() > 0 && (lastTick - fd.getTimeout()) > 60)
			{
				if (fd.getFlag() == FLAG_SERVER && fd.flag != 2)
					fd.setFlag(FLAG_PING);
				else
				{
					log("sessão #%d terminou (ip: %s).\n", fd.getID(), fd.getAddressString());
					fd.close();
				}
			}

			try {

				fd.getParseListener().onCall(fd);
				fd.setTimeout(60);

			} catch (RagnarokException e) {

				setUpSource(1);
				logError("processamento inválido encontrado:\n");
				logExeception(e);

			} catch (RagnarokRuntimeException e) {

				setUpSource(1);
				logError("informação inválida encontrada:\n");
				logExeception(e);

			} catch (Exception e) {

				setUpSource(1);
				logError("erro inesperado ocorrido:\n");
				logExeception(e);

			}
		}
	}
}

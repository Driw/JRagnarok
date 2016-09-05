package org.diverproject.jragnarok.packets;

import org.diverproject.jragnaork.RagnarokRuntimeException;

@SuppressWarnings("serial")
public class UnknowPacketException extends RagnarokRuntimeException
{
	public UnknowPacketException(String message)
	{
		super(message);
	}

	public UnknowPacketException(String format, Object... args)
	{
		super(format, args);
	}

	public UnknowPacketException(Exception e)
	{
		super(e);
	}


	public UnknowPacketException(Exception e, String format, Object... args)
	{
		super(e, format, args);
	}
}

package org.diverproject.jragnarok.server;

import java.net.Socket;

import org.diverproject.util.ObjectDescription;

public class ClientCharServer extends ClientServer
{
	private CharServerType type;
	private short newValue;

	public ClientCharServer(Socket socket)
	{
		super(socket);
	}

	public CharServerType getType()
	{
		return type;
	}

	public void setType(CharServerType type)
	{
		this.type = type;
	}

	public short getNewValue()
	{
		return newValue;
	}

	public void setNewValue(short newValue)
	{
		this.newValue = newValue;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("type", type);
		description.append("new", newValue);
	}
}

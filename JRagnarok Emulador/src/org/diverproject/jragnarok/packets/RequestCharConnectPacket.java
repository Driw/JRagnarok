package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;

import org.diverproject.util.stream.implementation.input.InputPacket;

public class RequestCharConnectPacket extends ReceivePacket
{
	private String username;
	private String password;
	private int serverIP;
	private short serverPort;
	private String serverName;
	private short type;
	private short newValue;

	@Override
	protected void receiveInput(InputPacket input)
	{
		username = input.getString(NAME_LENGTH);
		password = input.getString(NAME_LENGTH);
		input.skipe(4);
		serverIP = input.getInt();
		serverPort = input.getShort();
		serverName = input.getString(20);
		input.skipe(2);
		type = input.getShort();
		newValue = input.getShort();
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public int getServerIP()
	{
		return serverIP;
	}

	public short getServerPort()
	{
		return serverPort;
	}

	public String getServerName()
	{
		return serverName;
	}

	public short getType()
	{
		return type;
	}

	public short getNewValue()
	{
		return newValue;
	}

	@Override
	public String getName()
	{
		return "";
	}

	@Override
	public short getIdentify()
	{
		return 0;
	}
}

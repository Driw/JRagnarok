package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN2;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.implementation.input.InputPacket;

public class LoginMD5 extends ReceivePacket
{
	private int version;
	private String username;
	private String password;
	private byte clientType;

	@Override
	protected void receiveInput(InputPacket input)
	{
		version = input.getInt();
		username = input.getString(24);
		password = input.getString(16);
		clientType = input.getByte();
	}

	public int getVersion()
	{
		return version;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public byte getClientType()
	{
		return clientType;
	}

	@Override
	public String getName()
	{
		return "PACKET_CA_LOGIN2";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_LOGIN2;
	}
}

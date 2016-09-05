package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN2;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.implementation.input.InputPacket;

public class LoginMD5Mac extends ReceivePacket
{
	private int version;
	private String username;
	private String password;
	private byte clientType;
	private String macData;

	@Override
	protected void receiveInput(InputPacket input)
	{
		version = input.getInt();
		username = input.getString(24);
		password = input.getString(16);
		clientType = input.getByte();
		macData = input.getString(13);
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

	public String getMacData()
	{
		return macData;
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

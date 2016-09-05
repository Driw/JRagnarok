package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN_PCBANG;

import org.diverproject.util.stream.implementation.input.InputPacket;

public class LoginPCBang extends ReceivePacket
{
	private int version;
	private String username;
	private String password;
	private byte clientType;
	private String ip;
	private String macAddress;

	@Override
	protected void receiveInput(InputPacket input)
	{
		version = input.getInt();
		username = input.getString(24);
		password = input.getString(24);
		clientType = input.getByte();
		ip = input.getString(16);
		macAddress = input.getString(13);
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

	public String getIp()
	{
		return ip;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	@Override
	public String getName()
	{
		return "PACKET_CA_LOGIN_PCBANG";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_LOGIN_PCBANG;
	}
}

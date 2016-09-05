package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN_HAN;

import org.diverproject.util.stream.implementation.input.InputPacket;

public class LoginHan extends ReceivePacket
{
	private int version;
	private String username;
	private String password;
	private byte clientType;
	private String ip;
	private String macAddress;
	private boolean hanGameUser;

	@Override
	protected void receiveInput(InputPacket input)
	{
		version = input.getInt();
		username = input.getString(24);
		password = input.getString(24);
		clientType = input.getByte();
		ip = input.getString(16);
		macAddress = input.getString(13);		
		hanGameUser = input.getByte() == 0;
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

	public boolean isHanGameUser()
	{
		return hanGameUser;
	}

	@Override
	public String getName()
	{
		return "packet_CA_LOGIN_HAN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_LOGIN_HAN;
	}
}

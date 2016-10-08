package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_SSO_LOGIN_REQ;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class LoginSingleSignOn extends ReceivePacket
{
	private int version;
	private byte clientType;
	private String username;
	private String password;
	private String macAddress;
	private String ip;
	private String token;

	@Override
	protected void receiveInput(Input input)
	{
		short tokeLength = input.getShort();

		version = input.getInt();
		clientType = input.getByte();
		username = input.getString(24);
		password = input.getString(24);
		macAddress = input.getString(17);
		ip = input.getString(15);
		token = input.getString(tokeLength);
	}

	public int getVersion()
	{
		return version;
	}

	public byte getClientType()
	{
		return clientType;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	public String getIp()
	{
		return ip;
	}

	public String getToken()
	{
		return token;
	}

	@Override
	public String getName()
	{
		return "PACKET_CA_SSO_LOGIN_REQ";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_SSO_LOGIN_REQ;
	}

	@Override
	protected int length()
	{
		return 0;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("version", version);
		description.append("clientType", clientType);
		description.append("username", username);
		description.append("password", password);
		description.append("macAddress", macAddress);
		description.append("ip", ip);
		description.append("token", token);
	}
}

package org.diverproject.jragnarok.packets.receive;

import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LOGIN_HAN;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

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
	protected void receiveInput(Input input)
	{
		version = input.getInt();
		username = strclr(input.getString(24));
		password = strclr(input.getString(24));
		clientType = input.getByte();
		ip = strclr(input.getString(16));
		macAddress = strclr(input.getString(13));		
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
		return "LOGIN_HAN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_LOGIN_HAN;
	}

	@Override
	protected int length()
	{
		return 83;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("version", version);
		description.append("username", username);
		description.append("password", password);
		description.append("clientType", clientType);
		description.append("ip", ip);
		description.append("macAddress", macAddress);
		description.append("hanGameUser", hanGameUser);
	}
}
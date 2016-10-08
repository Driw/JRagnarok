package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class LoginPacket extends ReceivePacket
{
	private int version;
	private String username;
	private String password;
	private byte clientType;

	@Override
	protected void receiveInput(Input input)
	{
		version = input.getInt();
		username = input.getString(24);
		password = input.getString(24);
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
		return "PACKET_CA_LOGIN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_LOGIN;
	}

	@Override
	protected int length()
	{
		return 53;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("version", version);
		description.append("username", username);
		description.append("password", password);
		description.append("clientType", clientType);
	}
}

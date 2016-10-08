package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_LOGIN2;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class LoginMD5Mac extends ReceivePacket
{
	private int version;
	private String username;
	private String password;
	private byte clientType;
	private String macData;

	@Override
	protected void receiveInput(Input input)
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

	@Override
	protected int length()
	{
		return 58;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("version", version);
		description.append("username", username);
		description.append("password", password);
		description.append("clientType", clientType);
		description.append("macData", macData);
	}
}

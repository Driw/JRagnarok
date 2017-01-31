package org.diverproject.jragnarok.packets.login.fromclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN;
import static org.diverproject.util.Util.strclr;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CA_Login extends ReceivePacket
{
	private int version;
	private String username;
	private String password;
	private ClientType clientType;

	@Override
	protected void receiveInput(Input input)
	{
		version = input.getInt();
		username = strclr(input.getString(24));
		password = strclr(input.getString(24));
		clientType = ClientType.parse(input.getByte());
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

	public ClientType getClientType()
	{
		return clientType;
	}

	@Override
	public String getName()
	{
		return "CA_LOGIN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_LOGIN;
	}

	@Override
	protected int length()
	{
		return 55;
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

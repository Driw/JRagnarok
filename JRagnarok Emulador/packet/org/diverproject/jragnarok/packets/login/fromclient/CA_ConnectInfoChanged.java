package org.diverproject.jragnarok.packets.login.fromclient;

import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_CONNECT_INFO_CHANGED;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CA_ConnectInfoChanged extends ReceivePacket
{
	private String username;

	@Override
	protected void receiveInput(Input input)
	{
		username = strclr(input.getString(24));
	}

	public String getUsername()
	{
		return username;
	}

	@Override
	public String getName()
	{
		return "CA_CONNECT_INFO_CHANGED";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_CONNECT_INFO_CHANGED;
	}

	@Override
	protected int length()
	{
		return 26;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("username", username);
	}
}

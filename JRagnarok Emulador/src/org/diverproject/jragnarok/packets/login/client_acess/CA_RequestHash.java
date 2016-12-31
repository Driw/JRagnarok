package org.diverproject.jragnarok.packets.login.client_acess;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_REQ_HASH;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.Input;

public class CA_RequestHash extends ReceivePacket
{
	@Override
	protected void receiveInput(Input input)
	{
		
	}

	@Override
	public String getName()
	{
		return "CA_REQ_HASH";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_REQ_HASH;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_ACCEPT_DELETECHAR;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class HC_AcceptDeleteChar extends ResponsePacket
{
	@Override
	protected void sendOutput(Output output)
	{
		
	}

	@Override
	public String getName()
	{
		return "HC_ACCEPT_DELETECHAR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_ACCEPT_DELETECHAR;
	}

	@Override
	protected int length()
	{
		return 2;
	}
}

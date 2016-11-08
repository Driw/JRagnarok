package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_PING_RESPONSE;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class PingResponse extends ResponsePacket
{
	@Override
	protected void sendOutput(Output output)
	{
		
	}

	@Override
	protected int length()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "PACKET_PING_RESPONSE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_PING_RESPONSE;
	} 
}

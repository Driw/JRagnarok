package org.diverproject.jragnarok.packets.response;


import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_KEEP_ALIVE;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class KeepAliveResult extends ResponsePacket
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
		return "RES_KEEP_ALIVE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_RES_KEEP_ALIVE;
	} 
}

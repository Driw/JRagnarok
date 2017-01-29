package org.diverproject.jragnarok.packets.inter.charmap;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HZ_KEEP_ALIVE;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class HZ_KeepAlive extends ResponsePacket
{
	@Override
	protected void sendOutput(Output output)
	{
		
	}

	@Override
	public String getName()
	{
		return "AH_RES_KEEP_ALIVE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HZ_KEEP_ALIVE;
	}

	@Override
	protected int length()
	{
		return 2;
	}
}

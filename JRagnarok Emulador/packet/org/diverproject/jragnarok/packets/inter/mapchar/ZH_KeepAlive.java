package org.diverproject.jragnarok.packets.inter.mapchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ZH_KEEP_ALIVE;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class ZH_KeepAlive extends ResponsePacket
{
	@Override
	protected void sendOutput(Output output)
	{
		
	}

	@Override
	protected int length()
	{
		return 2;
	}

	@Override
	public String getName()
	{
		return "AH_KEEP_ALIVE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ZH_KEEP_ALIVE;
	}
}

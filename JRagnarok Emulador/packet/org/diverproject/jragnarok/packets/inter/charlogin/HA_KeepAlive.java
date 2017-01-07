package org.diverproject.jragnarok.packets.inter.charlogin;


import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_KEEP_ALIVE;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class HA_KeepAlive extends ResponsePacket
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
		return PACKET_HA_KEEP_ALIVE;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

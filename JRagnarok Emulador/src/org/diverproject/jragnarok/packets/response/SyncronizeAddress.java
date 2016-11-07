package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_SYNCRONIZE_IPADDRESS;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class SyncronizeAddress extends ResponsePacket
{
	@Override
	protected void sendOutput(Output output)
	{
		
	}

	@Override
	public String getName()
	{
		return "PACKET_SYNCRONIZE_IPADDRESS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SYNCRONIZE_IPADDRESS;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

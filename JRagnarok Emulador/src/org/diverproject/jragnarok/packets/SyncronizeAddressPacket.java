package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_SYNCRONIZE_IPADDRESS;

import org.diverproject.util.stream.implementation.output.OutputPacket;

public class SyncronizeAddressPacket extends ResponsePacket
{
	@Override
	protected void sendOutput(OutputPacket output)
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
}

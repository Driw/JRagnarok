package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_SYNCRONIZE_IPADDRESS;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class AH_SyncronizeAddress extends ResponsePacket
{
	@Override
	protected void sendOutput(Output output)
	{
		
	}

	@Override
	public String getName()
	{
		return "SYNCRONIZE_IPADDRESS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_SYNCRONIZE_IPADDRESS;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

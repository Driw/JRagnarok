package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_SET_ALL_ACC_OFFLINE;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_SetAllAccountOffline extends RequestPacket
{
	@Override
	protected void sendOutput(Output output)
	{
		
	}

	@Override
	protected void receiveInput(Input input)
	{
		
	}

	@Override
	public String getName()
	{
		return "HA_SET_ALL_ACC_OFFLINE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_SET_ALL_ACC_OFFLINE;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

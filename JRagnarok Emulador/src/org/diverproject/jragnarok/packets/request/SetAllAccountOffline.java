package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SET_ALL_ACC_OFFLINE;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class SetAllAccountOffline extends RequestPacket
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
		return "SET_ALL_ACC_OFFLINE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SET_ALL_ACC_OFFLINE;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

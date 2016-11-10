package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_UPDATE_USER_COUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class UpdateUserCount extends RequestPacket
{
	private int count;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(count);
	}

	@Override
	protected void receiveInput(Input input)
	{
		count = input.getInt();
	}

	public int getCount()
	{
		return count;
	}

	@Override
	public String getName()
	{
		return "UPDATE_USER_COUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_UPDATE_USER_COUNT;
	}

	@Override
	protected int length()
	{
		return 4;
	}
}

package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_UPDATE_USER_COUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_UpdateUserCount extends RequestPacket
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

	public void setCount(int count)
	{
		this.count = count;
	}

	@Override
	public String getName()
	{
		return "HA_UPDATE_USER_COUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_UPDATE_USER_COUNT;
	}

	@Override
	protected int length()
	{
		return 6;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("count", count);
	}
}

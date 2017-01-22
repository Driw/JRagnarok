package org.diverproject.jragnarok.packets.inter.mapchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ZH_NOTIFY_USER_COUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class ZH_NotifyUserCount extends RequestPacket
{
	private short userCount;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(userCount);
	}

	@Override
	protected void receiveInput(Input input)
	{
		userCount = input.getShort();
	}

	public short getUserCount()
	{
		return userCount;
	}

	public void setUserCount(short userCount)
	{
		this.userCount = userCount;
	}

	@Override
	public String getName()
	{
		return "PACKET_ZC_NOTIFY_USER_COUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ZH_NOTIFY_USER_COUNT;
	}

	@Override
	protected int length()
	{
		return 4;
	}

	@Override
	public void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("userCount", userCount);
	}
}

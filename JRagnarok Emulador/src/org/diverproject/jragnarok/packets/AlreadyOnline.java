package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_ALREADY_ONLINE;

import org.diverproject.util.stream.Output;

public class AlreadyOnline extends ResponsePacket
{
	private int accountID;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	@Override
	protected int length()
	{
		return 4;
	}

	@Override
	public String getName()
	{
		return "PACKET_ALREADY_ONLINE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ALREADY_ONLINE;
	}
}

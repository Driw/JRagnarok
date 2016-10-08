package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_CONNECT_INFO_CHANGED;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class KeepAlivePacket extends ReceivePacket
{
	private String identification;

	@Override
	protected void receiveInput(Input input)
	{
		identification = input.getString(24);
	}

	public String getIdentification()
	{
		return identification;
	}

	@Override
	public String getName()
	{
		return "PACKET_CA_CONNECT_INFO_CHANGED";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_CONNECT_INFO_CHANGED;
	}

	@Override
	protected int length()
	{
		return 24;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("identification", identification);
	}
}

package org.diverproject.jragnarok.packets.receive;

import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_KEEP_ALIVE;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class KeepAlive extends ReceivePacket
{
	private String identification;

	@Override
	protected void receiveInput(Input input)
	{
		identification = strclr(input.getString(24));
	}

	public String getIdentification()
	{
		return identification;
	}

	@Override
	public String getName()
	{
		return "PACKET_KEEP_ALIVE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_KEEP_ALIVE;
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

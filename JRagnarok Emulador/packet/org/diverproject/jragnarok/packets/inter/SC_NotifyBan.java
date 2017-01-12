package org.diverproject.jragnarok.packets.inter;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SC_NOTIFY_BAN;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.common.NotifyAuth;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class SC_NotifyBan extends ResponsePacket
{
	private NotifyAuth result;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result.CODE);
	}

	public void setResult(NotifyAuth result)
	{
		this.result = result;
	}

	@Override
	public String getName()
	{
		return "SC_NOTIFY_BAN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SC_NOTIFY_BAN;
	}

	@Override
	protected int length()
	{
		return 3;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("result", result);
	}
}

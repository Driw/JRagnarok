package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_NOTIFY_AUTH;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.login.structures.NotifyAuthResult;
import org.diverproject.util.stream.Output;

public class NotifyAuth extends ResponsePacket
{
	private NotifyAuthResult result;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result.CODE);
	}

	public void setResult(NotifyAuthResult result)
	{
		this.result = result;
	}

	@Override
	protected int length()
	{
		return 1;
	}

	@Override
	public String getName()
	{
		return "PACKET_NOTIFY_BAN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_NOTIFY_AUTH;
	}
}

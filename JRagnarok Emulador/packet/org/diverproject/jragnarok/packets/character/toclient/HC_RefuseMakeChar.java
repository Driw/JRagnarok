package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_REFUSE_MAKECHAR;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.common.RefuseMakeChar;
import org.diverproject.util.stream.Output;

public class HC_RefuseMakeChar extends ResponsePacket
{
	private RefuseMakeChar error;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(error.CODE);
	}

	public void setError(RefuseMakeChar error)
	{
		this.error = error;
	}

	@Override
	public String getName()
	{
		return "HC_REFUSE_MAKECHAR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_REFUSE_MAKECHAR;
	}

	@Override
	protected int length()
	{
		return 3;
	}
}

package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_REFUSE_DELETECHAR;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.common.RefuseDeleteChar;
import org.diverproject.util.stream.Output;

public class HC_RefuseDeleteChar extends ResponsePacket
{
	private RefuseDeleteChar error;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(error.CODE);
	}

	public void setError(RefuseDeleteChar error)
	{
		this.error = error;
	}

	@Override
	public String getName()
	{
		return "HC_REFUSE_DELETECHAR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_REFUSE_DELETECHAR;
	}

	@Override
	protected int length()
	{
		return 3;
	}
}

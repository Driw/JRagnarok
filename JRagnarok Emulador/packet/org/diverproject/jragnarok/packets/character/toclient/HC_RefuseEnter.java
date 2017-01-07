package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_REFUSE_ENTER;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.common.RefuseEnter;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class HC_RefuseEnter extends ResponsePacket
{
	private RefuseEnter error;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(error.CODE);
	}

	public void setError(RefuseEnter error)
	{
		this.error = error;
	}

	@Override
	public String getName()
	{
		return "HC_REFUSE_ENTER";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_REFUSE_ENTER;
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

		description.append("error", error);
	}
}

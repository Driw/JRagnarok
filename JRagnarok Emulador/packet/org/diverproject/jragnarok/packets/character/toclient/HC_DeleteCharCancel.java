package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_DELETE_CHAR3_CANCEL;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.common.DeleteCharCancel;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class HC_DeleteCharCancel extends ResponsePacket
{
	private int charID;
	private DeleteCharCancel result;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(charID);
		output.putInt(result.CODE);
	}

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	public void setResult(DeleteCharCancel result)
	{
		this.result = result;
	}

	@Override
	public String getName()
	{
		return "HC_DELETE_CHAR3_CANCEL";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_DELETE_CHAR3_CANCEL;
	}

	@Override
	protected int length()
	{
		return 10;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("charID", charID);
		description.append("result", result);
	}
}
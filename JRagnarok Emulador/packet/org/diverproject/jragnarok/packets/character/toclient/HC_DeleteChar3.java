package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_DELETE_CHAR3;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.common.DeleteChar;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class HC_DeleteChar3 extends ResponsePacket
{
	private int charID;
	private DeleteChar result;

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

	public void setResult(DeleteChar result)
	{
		this.result = result;
	}

	@Override
	public String getName()
	{
		return "HC_DELETE_CHAR3";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_DELETE_CHAR3;
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

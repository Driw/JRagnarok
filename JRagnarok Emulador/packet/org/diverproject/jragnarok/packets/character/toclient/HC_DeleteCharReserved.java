package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_DELETE_CHAR3_RESERVED;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.common.DeleteCharReserved;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class HC_DeleteCharReserved extends ResponsePacket
{
	private int charID;
	private DeleteCharReserved result;
	private int deleteDate;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(charID);
		output.putInt(result.CODE);
		output.putInt(deleteDate);
	}

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	public void setResult(DeleteCharReserved result)
	{
		this.result = result;
	}

	public void setDeleteDate(int deleteDate)
	{
		this.deleteDate = deleteDate;
	}

	@Override
	public String getName()
	{
		return "HC_DELETE_CHAR3_RESERVED";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_DELETE_CHAR3_RESERVED;
	}

	@Override
	protected int length()
	{
		return 14;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("charID", charID);
		description.append("result", result);
		description.append("deleteDate", deleteDate);
	}
}

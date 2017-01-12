package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR3_RESERVED;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CH_DeleteCharReserved extends ReceivePacket
{
	private int charID;

	@Override
	protected void receiveInput(Input input)
	{
		charID = input.getInt();
	}

	public int getCharID()
	{
		return charID;
	}

	@Override
	public String getName()
	{
		return "CH_DELETE_CHAR3_RESERVED";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_DELETE_CHAR3_RESERVED;
	}

	@Override
	protected int length()
	{
		return 6;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("charID",charID );
	}
}

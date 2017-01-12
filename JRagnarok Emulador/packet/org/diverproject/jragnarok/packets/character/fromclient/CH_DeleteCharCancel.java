package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR3_CANCEL;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.Input;

public class CH_DeleteCharCancel extends ReceivePacket
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
		return "CH_DELETE_CHAR3_CANCEL";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_DELETE_CHAR3_CANCEL;
	}

	@Override
	protected int length()
	{
		return 6;
	}
}

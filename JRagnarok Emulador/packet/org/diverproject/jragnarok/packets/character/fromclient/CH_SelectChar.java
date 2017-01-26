package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_SELECT_CHAR;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.Input;

public class CH_SelectChar extends ReceivePacket
{
	private byte slot;

	@Override
	protected void receiveInput(Input input)
	{
		slot = input.getByte();
	}

	public byte getSlot()
	{
		return slot;
	}

	@Override
	public String getName()
	{
		return "CH_SELECT_CHAR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_SELECT_CHAR;
	}

	@Override
	protected int length()
	{
		return 1;
	}
}

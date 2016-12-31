package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokConstants.PACKETVER;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_CHARLIST_NOTIFY;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class HC_CharListNotify extends ResponsePacket
{
	private int pageCount;
	private int charSlots;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(pageCount);

		if (PACKETVER >= 20151001)
			output.putInt(charSlots);
	}

	public void setPageCount(int pageCount)
	{
		this.pageCount = pageCount;
	}

	public void setCharSlots(int charSlots)
	{
		this.charSlots = charSlots;
	}

	@Override
	public String getName()
	{
		return "SEND_CHAR_PAGE_COUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_CHARLIST_NOTIFY;
	}

	@Override
	@SuppressWarnings("unused")
	protected int length()
	{
		return PACKETVER >= 20151001 ? 8 : 4;
	}
}

package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_CHARLIST_NOTIFY;
import static org.diverproject.util.lang.IntUtil.min;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class HC_CharListNotify extends ResponsePacket
{
	private int pageCount;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(pageCount);
	}

	public void setPageCount(int pageCount)
	{
		this.pageCount = min(pageCount, 1);
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
	protected int length()
	{
		return 4;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("pageCount", pageCount);
	}
}

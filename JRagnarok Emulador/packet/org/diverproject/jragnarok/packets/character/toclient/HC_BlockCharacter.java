package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_BLOCK_CHARACTER;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.Util.size;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.stream.Output;

public class HC_BlockCharacter extends ResponsePacket
{
	private Queue<TAG_CHARACTER_BLOCK_INFO> blocks;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(s(length()));

		while (!blocks.isEmpty())
		{
			TAG_CHARACTER_BLOCK_INFO block = blocks.poll();

			output.putInt(block.getCharID());
			output.putString(block.getUnbanTime(), TAG_CHARACTER_BLOCK_INFO.UNBAN_SIZE);
		}
	}

	public void setBlocks(Queue<TAG_CHARACTER_BLOCK_INFO> blocks)
	{
		this.blocks = blocks;
	}

	@Override
	public String getName()
	{
		return "HC_BLOCK_CHARACTER";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_BLOCK_CHARACTER;
	}

	@Override
	protected int length()
	{
		return 4 + (blocks.size() * TAG_CHARACTER_BLOCK_INFO.BYTES);
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("charBlocked", size(blocks));
	}
}

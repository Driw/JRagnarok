package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_BLOCK_CHARACTER;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.stream.Output;

public class HC_BlockCharacter extends ResponsePacket
{
	private Queue<CharBlock> blocks;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(s(length() + 2));

		while (!blocks.isEmpty())
		{
			CharBlock block = blocks.poll();

			output.putInt(block.getCharID());
			output.putString(block.getUnbanTime(), CharBlock.UNBAN_SIZE);
		}
	}

	public void setBlocks(Queue<CharBlock> blocks)
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
		return 2 + (blocks.size() * CharBlock.BYTES);
	}
}

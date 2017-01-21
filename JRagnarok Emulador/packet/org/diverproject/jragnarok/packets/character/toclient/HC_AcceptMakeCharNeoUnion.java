package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_ACCEPT_MAKECHAR_NEO_UNION;

import org.diverproject.jragnarok.packets.PacketStructures;
import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.common.entities.Character;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class HC_AcceptMakeCharNeoUnion extends ResponsePacket
{
	private boolean moveEnabled;
	private boolean moveUnlimited;
	private int moveCount;
	private byte slot;
	private Character character;

	@Override
	protected void sendOutput(Output output)
	{
		PacketStructures.CHARACTER_INFO_NEO_UNION(output, slot, character, moveEnabled, moveUnlimited, moveCount);
	}

	public void setMoveEnabled(boolean moveEnabled)
	{
		this.moveEnabled = moveEnabled;
	}

	public void setMoveUnlimited(boolean moveUnlimited)
	{
		this.moveUnlimited = moveUnlimited;
	}

	public void setMoveCount(int moveCount)
	{
		this.moveCount = moveCount;
	}

	public void setSlot(byte slot)
	{
		this.slot = slot;
	}

	public void setCharacter(Character character)
	{
		this.character = character;
	}

	@Override
	public String getName()
	{
		return "HC_ACCEPT_MAKECHAR_NEO_UNION";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_ACCEPT_MAKECHAR_NEO_UNION;
	}

	@Override
	protected int length()
	{
		return 2 + Character.BYTES;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("slot", slot);
		description.append("character", character == null ? null : character.getName());

		if (moveEnabled)
		{
			description.append("moveUnlimited", moveUnlimited);
			description.append("moveCount", moveCount);
		}
	}
}

package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_ACK_CHARINFO_PER_PAGE;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.Util.size;

import org.diverproject.jragnarok.packets.PacketStructures;
import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.common.entities.Character;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Index;
import org.diverproject.util.stream.Output;

public class HC_AckCharInfoPerPage extends ResponsePacket
{
	private boolean charMoveEnabled;
	private boolean charMoveUnlimited;
	private int charMoveCount;
	private Index<Character> characters;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(s(length()));

		for (int slot = 0; slot < MAX_CHARS; slot++)
			if (characters.get(slot) != null)
				PacketStructures.CHARACTER_INFO_NEO_UNION(output, slot, characters.get(slot), charMoveEnabled, charMoveUnlimited, charMoveCount);
	}

	public void setCharMoveEnabled(boolean charMoveEnabled)
	{
		this.charMoveEnabled = charMoveEnabled;
	}

	public void setCharMoveUnlimited(boolean charMoveUnlimited)
	{
		this.charMoveUnlimited = charMoveUnlimited;
	}

	public void setCharMoveCount(int charMoveCount)
	{
		this.charMoveCount = charMoveCount;
	}

	public void setCharacters(Index<Character> characters)
	{
		this.characters = characters;
	}

	@Override
	public String getName()
	{
		return "HC_ACK_CHARINFO_PER_PAGE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_ACK_CHARINFO_PER_PAGE;
	}

	@Override
	protected int length()
	{
		return 4 + (characters.size() * Character.BYTES);
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("charMoveEnabled", charMoveEnabled);
		description.append("charMoveUnlimited", charMoveUnlimited);
		description.append("charMoveCount", charMoveCount);
		description.append("characters", size(characters));
	}
}

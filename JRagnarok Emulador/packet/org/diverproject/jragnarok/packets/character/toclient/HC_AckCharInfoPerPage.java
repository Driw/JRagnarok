package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_ACK_CHARINFO_PER_PAGE;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.character.entities.Character;
import org.diverproject.util.collection.Index;
import org.diverproject.util.stream.Output;

public class HC_AckCharInfoPerPage extends ResponsePacket
{
	private Index<Character> characters;
	private boolean charMoveEnabled;
	private boolean charMoveUnlimited;
	private int charMoveCount;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(s(length() - 2));

		for (int slot = 0; slot < MAX_CHARS; slot++)
			if (characters.get(slot) != null)
				CharacterInfo.put(output, slot, characters.get(slot), charMoveEnabled, charMoveUnlimited, charMoveCount);
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
		return (Character.BYTES * characters.size()) + 2;
	}
}

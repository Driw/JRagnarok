package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.JRagnarokConstants.DEFAULT_WALK_SPEED;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_NAME_LENGTH_EXT;
import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.i;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SEND_CHARS_PER_PAGE;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.character.structures.Character;
import org.diverproject.util.collection.Index;
import org.diverproject.util.stream.Output;

public class SendCharsPerPage extends ResponsePacket
{
	private Index<Character> characters;
	private boolean charMoveEnabled;
	private boolean charMoveUnlimited;
	private int charMoveCount;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(s(length() - 2));

		for (int slot = 0; slot < characters.size(); slot++)
		{
			Character character = characters.get(slot);

			if (character == null)
				continue;

			output.putInt(character.getID());
			output.putInt(character.getExperience().getBase());
			output.putInt(character.getZeny());
			output.putInt(character.getExperience().getJob());
			output.putInt(character.getJobLevel());
			output.putInt(0); // bodystate : probably opt1
			output.putInt(0); // healthstate : probably opt2
			output.putInt(character.getEffectState().getValue());
			output.putInt(character.getManner());
			output.putInt(character.getKarma());
			output.putShort(character.getStatusPoint());
			output.putInt(character.getHP());
			output.putInt(character.getMaxHP());
			output.putShort(character.getSP());
			output.putShort(character.getMaxSP());
			output.putShort(s(DEFAULT_WALK_SPEED));
			output.putShort(character.getJobID());
			output.putShort(character.getLook().getHair());
			output.putShort(character.getLook().getBody());

			int propertie = 0x20|0x80000|0x100000|0x200000|0x400000|0x800000|0x1000000|0x2000000|0x4000000|0x8000000;

			output.putShort(character.getEffectState().is(propertie) ? s(0) : character.getLook().getWeapon());
			output.putShort(s(character.getBaseLevel()));
			output.putShort(character.getSkillPoint());
			output.putShort(character.getLook().getHeadBottom());
			output.putShort(character.getLook().getShield());
			output.putShort(character.getLook().getHeadTop());
			output.putShort(character.getLook().getHeadMid());
			output.putShort(character.getLook().getHairColor());
			output.putByte(b(character.getLook().getClothesColor()));
			output.putString(character.getName(), NAME_LENGTH);
			output.putByte(b(character.getStats().getStrength()));
			output.putByte(b(character.getStats().getAgility()));
			output.putByte(b(character.getStats().getVitality()));
			output.putByte(b(character.getStats().getIntelligence()));
			output.putByte(b(character.getStats().getDexterity()));
			output.putByte(b(character.getStats().getLuck()));
			output.putByte(b(slot));
			output.putShort(s(character.getRename() > 0 ? 0 : 1));
			output.putString(character.getLocations().getSavePoint().getMap(), MAP_NAME_LENGTH_EXT);
			output.putInt(i(character.getDeleteDate().get()));
			output.putInt(character.getLook().getRobe());
			output.putInt(!charMoveEnabled ? 0 : charMoveUnlimited ? 1 : charMoveCount);
			output.putInt(character.getRename() > 0 ? 1 : 0);
			output.putByte(Character.intSexOf(character.getSex()));
		}
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
		return "PACKET_SEND_CHARS_PER_PAGE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SEND_CHARS_PER_PAGE;
	}

	@Override
	protected int length()
	{
		return (Character.PACKET_BYTES * characters.size()) + 2;
	}
}

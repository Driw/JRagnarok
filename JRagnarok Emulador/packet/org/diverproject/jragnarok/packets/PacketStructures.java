package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.JRagnarokConstants.DEFAULT_WALK_SPEED;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_NAME_LENGTH_EXT;
import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.i;
import static org.diverproject.jragnarok.JRagnarokUtil.s;

import org.diverproject.jragnarok.server.character.entities.Character;
import org.diverproject.util.stream.Output;

public class PacketStructures
{
	public static final void CHARACTER_INFO_NEO_UNION(Output output, int slot, Character character, boolean moveEnabled, boolean moveUnlimited, int moveCount)
	{
		output.putInt(character.getID());
		output.putInt(character.getExperience().getBase());
		output.putInt(character.getZeny());
		output.putInt(character.getExperience().getJob());
		output.putInt(character.getJobLevel());
		output.putInt(0); // bodystate : probably opt1
		output.putInt(0); // healthstate : probably opt2
		output.putInt(character.getEffectState().getValue());
		output.putInt(character.getVirtue());
		output.putInt(character.getExperience().getFame()); // TODO fama ou mudo?
		output.putShort(character.getStatusPoint());
		output.putInt(character.getHP());
		output.putInt(character.getMaxHP());
		output.putShort(character.getSP());
		output.putShort(character.getMaxSP());
		output.putShort(s(DEFAULT_WALK_SPEED));
		output.putShort(character.getJob().CODE);
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
		output.putShort(character.getLook().getClothesColor());
		output.putString(character.getName(), NAME_LENGTH);
		output.putByte(b(character.getStats().getStrength()));
		output.putByte(b(character.getStats().getAgility()));
		output.putByte(b(character.getStats().getVitality()));
		output.putByte(b(character.getStats().getIntelligence()));
		output.putByte(b(character.getStats().getDexterity()));
		output.putByte(b(character.getStats().getLuck()));
		output.putShort(s(slot));
		output.putShort(s(character.getRename() > 0 ? 0 : 1));
		output.putString(character.getLocations().getSavePoint().getMap()+ ".gat", MAP_NAME_LENGTH_EXT); // TODO pegar de outro lugar
		output.putInt(i(character.getDeleteDate().get()));
		output.putInt(character.getLook().getRobe());
		output.putInt(!moveEnabled ? 0 : moveUnlimited ? 1 : moveCount);
		output.putInt(character.getRename() > 0 ? 1 : 0);
		output.putByte(character.getSex().code());
	}
}

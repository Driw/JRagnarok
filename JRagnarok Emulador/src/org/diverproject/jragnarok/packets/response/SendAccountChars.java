package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.JRagnarokConstants.DEFAULT_WALK_SPEED;
import static org.diverproject.jragnarok.JRagnarokConstants.MAP_NAME_LENGTH_EXT;
import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.PACKETVER;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.i;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SEND_ACCOUNT_CHARS;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.character.structures.Character;
import org.diverproject.util.collection.Index;
import org.diverproject.util.stream.Output;

public class SendAccountChars extends ResponsePacket
{
	private boolean charMoveEnabled;
	private boolean charMoveUnlimited;
	private int charMoveCount;

	private byte totalSlots;
	private byte premiumStartSlot;
	private byte premiumEndSlot;
	private byte dummyBeginBilling;
	private int code;
	private int firstTime;
	private int secondTime;
	private String dummyEndBilling;
	private Index<Character> characters;

	@Override
	protected void sendOutput(Output output)
	{
		short length = s(calcLength());

		output.putShort(length);
		output.putByte(totalSlots);
		output.putByte(premiumStartSlot);
		output.putByte(premiumEndSlot);
		output.putByte(dummyBeginBilling);
		output.putInt(code);
		output.putInt(firstTime);
		output.putInt(secondTime);
		output.putBytes(dummyEndBilling.getBytes());

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

			if (PACKETVER >= 20141022)
				output.putShort(character.getLook().getBody());

			int propertie = 0x20|0x80000|0x100000|0x200000|0x400000|0x800000|0x1000000|0x2000000|0x4000000|0x8000000;

			output.putShort(character.getEffectState().is(propertie) ? s(0) : character.getLook().getWeapon());
			output.putShort(character.getLook().getWeapon());
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
			output.putByte(b(slot));
			output.putByte(b(character.getLook().getHairColor()));
			output.putShort(s(character.getRename() > 0 ? 0 : 1));

			if (interval(PACKETVER, 20100720, 20100727) || PACKETVER >= 20100803)
				output.putString(character.getLocations().getSavePoint().getMap(), MAP_NAME_LENGTH_EXT);

			if (PACKETVER >= 20100803)
				output.putInt(i(character.getDeleteDate().get()));

			if (PACKETVER >= 20110111)
				output.putInt(character.getLook().getRobe());

			if (PACKETVER >= 20110928 && PACKETVER != 20111116)
			{
				if (!charMoveEnabled)
					output.skipe(4);
				else if (charMoveUnlimited)
					output.putInt(1);
				else
					output.putInt(charMoveCount);

				if (PACKETVER >= 20111025)
					output.putInt(character.getRename() > 0 ? 1 : 0);

				if (PACKETVER >= 20141016)
					output.putChar(character.getSex());
			}
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

	public void setTotalSlots(byte totalSlots)
	{
		this.totalSlots = totalSlots;
	}

	public void setPremiumStartSlot(byte premiumStartSlot)
	{
		this.premiumStartSlot = premiumStartSlot;
	}

	public void setPremiumEndSlot(byte premiumEndSlot)
	{
		this.premiumEndSlot = premiumEndSlot;
	}

	public void setDummyBeginBilling(byte dummyBeginBilling)
	{
		this.dummyBeginBilling = dummyBeginBilling;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public void setFirstTime(int firstTime)
	{
		this.firstTime = firstTime;
	}

	public void setSecondTime(int secondTime)
	{
		this.secondTime = secondTime;
	}

	public void setDummyEndBilling(String dummyEndBilling)
	{
		this.dummyEndBilling = dummyEndBilling;
	}

	public void setCharacters(Index<Character> characters)
	{
		this.characters = characters;
	}

	private int calcLength()
	{
		return 23 + (Character.PACKET_BYTES * characters.size());
	}

	@Override
	public String getName()
	{
		return "SEND_ACCOUNT_CHARS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SEND_ACCOUNT_CHARS;
	}

	@Override
	protected int length()
	{
		return calcLength() + 2;
	}
}
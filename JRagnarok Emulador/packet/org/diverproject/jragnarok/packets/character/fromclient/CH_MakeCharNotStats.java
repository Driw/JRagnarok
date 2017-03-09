package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_MAKE_CHAR_NOT_STATS;
import static org.diverproject.util.Util.strclr;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CH_MakeCharNotStats extends ReceivePacket
{
	private String charName;
	private byte slot;
	private short hairColor;
	private short clothesColor;

	@Override
	protected void receiveInput(Input input)
	{
		charName = strclr(input.getString(NAME_LENGTH));
		slot = input.getByte();
		hairColor = input.getShort();
		clothesColor = input.getShort();
	}

	public String getCharName()
	{
		return charName;
	}

	public byte getSlot()
	{
		return slot;
	}

	public short getHairColor()
	{
		return hairColor;
	}

	public short getClothesColor()
	{
		return clothesColor;
	}

	@Override
	public String getName()
	{
		return "CH_MAKE_CHAR_NOT_STATS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_MAKE_CHAR_NOT_STATS;
	}

	@Override
	protected int length()
	{
		return 31;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("name", charName);
		description.append("slot", slot);
		description.append("hairColor", hairColor);
		description.append("clothesColor", clothesColor);
	}
}

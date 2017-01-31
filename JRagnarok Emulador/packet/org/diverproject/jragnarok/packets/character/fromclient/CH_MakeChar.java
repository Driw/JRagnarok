package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_MAKE_CHAR;
import static org.diverproject.util.Util.strclr;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CH_MakeChar extends ReceivePacket
{
	private String charName;
	private byte strength;
	private byte agility;
	private byte vitality;
	private byte intelligence;
	private byte dexterity;
	private byte luck;
	private byte slot;
	private short hairColor;
	private short clothesColor;

	@Override
	protected void receiveInput(Input input)
	{
		charName = strclr(input.getString(NAME_LENGTH));
		strength = input.getByte();
		agility = input.getByte();
		vitality = input.getByte();
		intelligence = input.getByte();
		dexterity = input.getByte();
		luck = input.getByte();
		slot = input.getByte();
		hairColor = input.getShort();
		clothesColor = input.getShort();
	}

	public String getCharName()
	{
		return charName;
	}

	public byte getStrength()
	{
		return strength;
	}

	public byte getAgility()
	{
		return agility;
	}

	public byte getVitality()
	{
		return vitality;
	}

	public byte getIntelligence()
	{
		return intelligence;
	}

	public byte getDexterity()
	{
		return dexterity;
	}

	public byte getLuck()
	{
		return luck;
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
		return "CH_MAKE_CHAR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_MAKE_CHAR;
	}

	@Override
	protected int length()
	{
		return 37;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("name", charName);
		description.append("strengh", strength);
		description.append("agility", agility);
		description.append("vitality", vitality);
		description.append("intelligence", intelligence);
		description.append("dexterity", dexterity);
		description.append("luck", luck);
		description.append("slot", slot);
		description.append("hairColor", hairColor);
		description.append("clothesColor", clothesColor);
	}
}

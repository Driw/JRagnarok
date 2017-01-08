package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_CREATE_NEW_CHAR;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CH_CreateNewChar extends ReceivePacket
{
	private String charName;
	private byte slot;
	private short hairColor;
	private short hairStyle;
	private short startJob;
	private short unknow;
	private Sex sex;

	@Override
	protected void receiveInput(Input input)
	{
		charName = strclr(input.getString(NAME_LENGTH));
		slot = input.getByte();
		hairColor = input.getShort();
		hairStyle = input.getShort();
		startJob = input.getShort();
		unknow = input.getShort();
		sex = Sex.parse(input.getByte());
	}

	public String getCharName()
	{
		return charName;
	}

	public void setCharName(String charName)
	{
		this.charName = charName;
	}

	public byte getSlot()
	{
		return slot;
	}

	public void setSlot(byte slot)
	{
		this.slot = slot;
	}

	public short getHairColor()
	{
		return hairColor;
	}

	public void setHairColor(short hairColor)
	{
		this.hairColor = hairColor;
	}

	public short getHairStyle()
	{
		return hairStyle;
	}

	public void setHairStyle(short hairStyle)
	{
		this.hairStyle = hairStyle;
	}

	public short getStartJob()
	{
		return startJob;
	}

	public void setStartJob(short startJob)
	{
		this.startJob = startJob;
	}

	public Sex getSex()
	{
		return sex;
	}

	public void setSex(Sex sex)
	{
		this.sex = sex;
	}

	@Override
	public String getName()
	{
		return "CH_CREATE_NEW_CHAR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_CREATE_NEW_CHAR;
	}

	@Override
	protected int length()
	{
		return 36;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("name", charName);
		description.append("slot", slot);
		description.append("hairColor", hairColor);
		description.append("hairStyle", hairStyle);
		description.append("startJob", startJob);
		description.append("unknow", unknow);
		description.append("sex", sex);
	}
}

package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR3;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.Input;

public class CH_DeleteChar3 extends ReceivePacket
{
	private int charID;
	private String birthDate;

	@Override
	protected void receiveInput(Input input)
	{
		charID = input.getInt();
		birthDate = strclr(input.getString(6));
	}

	public int getCharID()
	{
		return charID;
	}

	public String getBirthDate()
	{
		return birthDate;
	}

	@Override
	public String getName()
	{
		return "CH_DELETE_CHAR3";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_DELETE_CHAR3;
	}

	@Override
	protected int length()
	{
		return 12;
	}
}

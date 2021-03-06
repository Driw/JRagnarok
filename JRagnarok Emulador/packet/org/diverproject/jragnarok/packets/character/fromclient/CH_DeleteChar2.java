package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR;
import static org.diverproject.util.Util.strclr;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.Input;

public class CH_DeleteChar2 extends ReceivePacket
{
	private int charID;
	private String email;

	@Override
	protected void receiveInput(Input input)
	{
		charID = input.getInt();
		email = strclr(input.getString(50));
	}

	public int getCharID()
	{
		return charID;
	}

	public String getEmail()
	{
		return email;
	}

	@Override
	public String getName()
	{
		return "CH_DELETE_CHAR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_DELETE_CHAR;
	}

	@Override
	protected int length()
	{
		return 56;
	}
}

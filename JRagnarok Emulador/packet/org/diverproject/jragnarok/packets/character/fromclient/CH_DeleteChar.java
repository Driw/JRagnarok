package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.Input;

public class CH_DeleteChar extends ReceivePacket
{
	private int charID;
	private String email;

	@Override
	protected void receiveInput(Input input)
	{
		charID = input.getInt();
		email = strclr(input.getString(EMAIL_LENGTH));
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
		return 46;
	}
}

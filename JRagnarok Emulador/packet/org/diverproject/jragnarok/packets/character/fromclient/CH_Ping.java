package org.diverproject.jragnarok.packets.character.fromclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_PING;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.stream.Input;

public class CH_Ping extends ReceivePacket
{
	private int accountID;

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
	}

	public int getAccountID()
	{
		return accountID;
	}

	@Override
	public String getName()
	{
		return "CH_PING";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CH_PING;
	}

	@Override
	protected int length()
	{
		return 6;
	}
}

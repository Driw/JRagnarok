package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_ALREADY_ONLINE;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_AlreadyOnline extends RequestPacket
{
	private int accountID;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	@Override
	protected int length()
	{
		return 4;
	}

	@Override
	public String getName()
	{
		return "AH_ALREADY_ONLINE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_ALREADY_ONLINE;
	}
}

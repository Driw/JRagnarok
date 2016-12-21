package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_UNBAN_ACCOUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class UnbanAccount extends RequestPacket
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
	public String getName()
	{
		return "REQ_UNBAN_ACCOUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REQ_UNBAN_ACCOUNT;
	}

	@Override
	protected int length()
	{
		return 4;
	}
}

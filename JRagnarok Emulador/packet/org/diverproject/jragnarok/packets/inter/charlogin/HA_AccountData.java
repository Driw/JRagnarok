package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_DATA;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_AccountData extends RequestPacket
{
	private int fdID;
	private int accountID;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(fdID);
		output.putInt(accountID);
	}

	@Override
	protected void receiveInput(Input input)
	{
		fdID = input.getInt();
		accountID = input.getInt();
	}

	public int getFdID()
	{
		return fdID;
	}

	public void setFdID(int fdID)
	{
		this.fdID = fdID;
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
		return "HA_ACCOUNT_DATA";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_ACCOUNT_DATA;
	}

	@Override
	protected int length()
	{
		return 8;
	}
}

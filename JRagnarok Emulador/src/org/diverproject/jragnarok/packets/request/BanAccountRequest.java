package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ACCOUNT_STATE_NOTIFY;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class BanAccountRequest extends RequestPacket
{
	private int accountID;
	private int durationTime;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInts(accountID, durationTime);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		durationTime = input.getInt();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public int getDurationTime()
	{
		return durationTime;
	}

	public void setDurationTime(int durationTime)
	{
		this.durationTime = durationTime;
	}

	@Override
	public String getName()
	{
		return "PACKET_REQ_BAN_ACCOUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ACCOUNT_STATE_NOTIFY;
	}

	@Override
	protected int length()
	{
		return 8;
	}
}

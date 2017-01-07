package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_STATE_NOTIFY;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_BanAccount extends RequestPacket
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
		return "HA_BAN_ACCOUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_ACCOUNT_STATE_NOTIFY;
	}

	@Override
	protected int length()
	{
		return 10;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("durationTime", durationTime);
	}
}

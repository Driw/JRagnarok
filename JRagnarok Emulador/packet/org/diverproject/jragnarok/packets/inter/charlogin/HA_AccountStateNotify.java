package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.util.Util.b;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_STATE_NOTIFY;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_AccountStateNotify extends RequestPacket
{
	private int accountID;
	private boolean banned;
	private int value;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putByte(b(banned ? 1 : 0));
		output.putInt(value);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		banned = input.getByte() == 1;
		value = input.getInt();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public boolean isBanned()
	{
		return banned;
	}

	public void setBanned(boolean banned)
	{
		this.banned = banned;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		if (value >= 0)
			this.value = value;
	}

	@Override
	public String getName()
	{
		return "HA_ACCOUNT_STATE_NOTIFY";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_ACCOUNT_STATE_NOTIFY;
	}

	@Override
	protected int length()
	{
		return 11;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("type", banned);
		description.append("value", value);
	}
}

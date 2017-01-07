package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_STATE_NOTIFY;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_AccountStateNotify extends RequestPacket
{
	public static final byte CHANGE_STATE = 0;
	public static final byte BAN = 1;

	private int accountID;
	private byte type;
	private int value;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putByte(type);
		output.putInt(value);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		type = input.getByte();
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

	public byte getType()
	{
		return type;
	}

	public void setType(byte type)
	{
		this.type = type;
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
		description.append("type", type);
		description.append("value", value);
	}
}

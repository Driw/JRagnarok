package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_BAN_NOTIFICATION;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.packets.common.BanNotification;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_BanNotification extends RequestPacket
{
	private int accountID;
	private int unbanTime;
	private BanNotification type;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putByte(type.CODE);
		output.putInt(unbanTime);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		type = BanNotification.parse(input.getByte());
		unbanTime = input.getInt();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public int getUnbanTime()
	{
		return unbanTime;
	}

	public void setUnbanTime(int unbanTime)
	{
		this.unbanTime = unbanTime;
	}

	public BanNotification getType()
	{
		return type;
	}

	public void setType(BanNotification type)
	{
		this.type = type;
	}

	@Override
	public String getName()
	{
		return "AH_BAN_NOTIFICATION";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_BAN_NOTIFICATION;
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

		description.append("accoutnID", accountID);
		description.append("unbanTime", unbanTime);
		description.append("type", type);
	}
}

package org.diverproject.jragnarok.packets.inter.charmap;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HZ_BAN;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_BanNotification.BanNotificationType;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HZ_Ban extends RequestPacket
{
	private int accountID;
	private int unbanTime;
	private BanNotificationType type;

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
		type = BanNotificationType.parse(input.getByte());
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

	public BanNotificationType getType()
	{
		return type;
	}

	public void setType(BanNotificationType type)
	{
		this.type = type;
	}

	@Override
	public String getName()
	{
		return "HZ_BAN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HZ_BAN;
	}

	@Override
	protected int length()
	{
		return 9;
	}
}

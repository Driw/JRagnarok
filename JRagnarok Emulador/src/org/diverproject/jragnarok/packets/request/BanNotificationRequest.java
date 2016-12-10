package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_BAN_NOTIFICATION;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class BanNotificationRequest extends RequestPacket
{
	public enum BanNotificationType
	{
		CHANGE_OF_STATUS(0),
		BAN(1);

		public final byte CODE;

		private BanNotificationType(int code)
		{
			CODE = b(code);
		}

		public static BanNotificationType parse(byte b)
		{
			switch (b)
			{
				case 0: return CHANGE_OF_STATUS;
				case 1: return BAN;
			}

			throw new RagnarokRuntimeException("BanNotificationType#%d não encontrado", b);
		}
	}

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
		return "REQ_BAN_NOTIFICATION";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REQ_BAN_NOTIFICATION;
	}

	@Override
	protected int length()
	{
		return 9;
	}
}

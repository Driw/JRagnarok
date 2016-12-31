package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_SECOND_PASSWD_LOGIN;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class HC_SecondPasswordLogin extends ResponsePacket
{
	public static enum PincodeState
	{
		OK(0),
		ASK(1),
		NOTSET(2),
		EXPIRED(3),
		NEW(4),
		ILLEGAL(5),
		KSSN(6),
		SKIP(7),
		WRONG(8);

		public final short CODE;

		private PincodeState(int code)
		{
			CODE = s(code);
		}
	}

	private int pincodeSeed;
	private int accountID;
	private short state;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(pincodeSeed);
		output.putInt(accountID);
		output.putShort(state);
	}

	public void setPincodeSeed(int pincodeSeed)
	{
		this.pincodeSeed = pincodeSeed;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public void setState(short state)
	{
		this.state = state;
	}

	@Override
	public String getName()
	{
		return "HC_SECOND_PASSWD_LOGIN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_SECOND_PASSWD_LOGIN;
	}

	@Override
	protected int length()
	{
		return 10;
	}
}

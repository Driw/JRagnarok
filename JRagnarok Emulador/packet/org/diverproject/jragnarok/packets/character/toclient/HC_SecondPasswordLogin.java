package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_SECOND_PASSWD_LOGIN;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class HC_SecondPasswordLogin extends ResponsePacket
{
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
		return 12;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("pincodeSeed", pincodeSeed);
		description.append("accountID", accountID);
		description.append("state", state);
	}
}

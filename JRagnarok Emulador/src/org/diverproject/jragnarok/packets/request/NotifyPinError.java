package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_NOTIFY_PIN_ERROR;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class NotifyPinError extends RequestPacket
{
	private int accountID;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
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
		return "NOTIFY_PIN_ERROR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_NOTIFY_PIN_ERROR;
	}

	@Override
	protected int length()
	{
		return 4;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);

		return description.toString();
	}
}

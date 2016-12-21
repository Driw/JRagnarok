package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.JRagnarokConstants.PINCODE_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_NOTIFY_PIN_UPDATE;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class NotifyPinUpdate extends RequestPacket
{
	private int accountID;
	private String pincode;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putString(pincode, PINCODE_LENGTH);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		pincode = input.getString();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public String getPincode()
	{
		return pincode;
	}

	public void setPincode(String pincode)
	{
		this.pincode = strcap(pincode, PINCODE_LENGTH);
	}

	@Override
	public String getName()
	{
		return "NOTIFY_PIN_UPDATE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_NOTIFY_PIN_UPDATE;
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
		description.append("pincode", pincode);

		return description.toString();
	}
}

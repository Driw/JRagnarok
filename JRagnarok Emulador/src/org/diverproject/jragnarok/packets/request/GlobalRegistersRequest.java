package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_GLOBAL_REGISTER;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class GlobalRegistersRequest extends RequestPacket
{
	private int accountID;
	private int charID;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInts(accountID, charID);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		charID = input.getInt();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public int getCharID()
	{
		return charID;
	}

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	@Override
	public String getName()
	{
		return "REQ_GLOBAL_REGISTER";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REQ_GLOBAL_REGISTER;
	}

	@Override
	protected int length()
	{
		return 8;
	}
}

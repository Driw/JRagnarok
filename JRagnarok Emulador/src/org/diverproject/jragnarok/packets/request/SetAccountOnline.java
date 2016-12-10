package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SET_ACCOUNT_ONLINE;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class SetAccountOnline extends RequestPacket
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

	/**
	 * @return aquisição do código de identificação da conta.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @param accountID código de identificação da conta.
	 */

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	@Override
	public String getName()
	{
		return "SET_ACCOUNT_ONLINE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SET_ACCOUNT_ONLINE;
	}

	@Override
	protected int length()
	{
		return 4;
	}
}

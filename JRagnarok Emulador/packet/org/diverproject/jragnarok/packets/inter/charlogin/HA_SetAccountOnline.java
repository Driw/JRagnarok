package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_SET_ACCOUNT_ONLINE;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_SetAccountOnline extends RequestPacket
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
	 * @return aquisi��o do c�digo de identifica��o da conta.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @param accountID c�digo de identifica��o da conta.
	 */

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	@Override
	public String getName()
	{
		return "HA_SET_ACCOUNT_ONLINE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_SET_ACCOUNT_ONLINE;
	}

	@Override
	protected int length()
	{
		return 6;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
	}
}

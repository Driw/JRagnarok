package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_STATE_UPDATE;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.login.entities.AccountState;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_AccountStateUpdate extends RequestPacket
{
	private int accountID;
	private AccountState accountState;

	public HA_AccountStateUpdate()
	{
		accountState = AccountState.NONE;
	}

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putInt(accountState.CODE);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		accountState = AccountState.parse(input.getInt());
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public AccountState getAccountState()
	{
		return accountState;
	}

	public void setAccountState(AccountState accountState)
	{
		if (accountState != null)
			this.accountState = accountState;
	}

	@Override
	public String getName()
	{
		return "HA_ACCOUNT_STATE_UPDATE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_ACCOUNT_STATE_UPDATE;
	}

	@Override
	protected int length()
	{
		return 10;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("accountState", accountState);
	}
}

package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ACCOUNT_STATE_UPDATE;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.login.entities.AccountState;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class UpdateAccountState extends RequestPacket
{
	private int accountID;
	private AccountState accountState;

	public UpdateAccountState()
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
		return "ACCOUNT_STATE_UPDATE";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ACCOUNT_STATE_UPDATE;
	}

	@Override
	protected int length()
	{
		return 8;
	}
}

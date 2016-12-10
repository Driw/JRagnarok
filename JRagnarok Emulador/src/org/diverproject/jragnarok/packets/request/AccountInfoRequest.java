package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_ACCOUNT_INFO;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AccountInfoRequest extends RequestPacket
{
	private int ServerFD;
	private int userFD;
	private int accountID;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(ServerFD);
		output.putInt(userFD);
		output.putInt(accountID);
	}

	@Override
	protected void receiveInput(Input input)
	{
		ServerFD = input.getInt();
		userFD = input.getInt();
		accountID = input.getInt();
	}

	public int getServerFD()
	{
		return ServerFD;
	}

	public void setServerFD(int mapfd)
	{
		if (mapfd > 0)
			this.ServerFD = mapfd;
	}

	public int getUFD()
	{
		return userFD;
	}

	public void setUserFD(int userFD)
	{
		if (userFD > 0)
			this.userFD = userFD;
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		if (accountID > 0)
			this.accountID = accountID;
	}

	@Override
	public String getName()
	{
		return "REQ_ACCOUNT_INFO";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REQ_ACCOUNT_INFO;
	}

	@Override
	protected int length()
	{
		return 12;
	}
}

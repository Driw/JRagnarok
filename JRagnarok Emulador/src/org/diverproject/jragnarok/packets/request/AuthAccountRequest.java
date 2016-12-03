package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_AUTH_ACCOUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AuthAccountRequest extends RequestPacket
{
	private int accountID;
	private int firstSeed;
	private int secondSeed;
	private int ip;
	private int fdID;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putInt(firstSeed);
		output.putInt(secondSeed);
		output.putInt(ip);
		output.putInt(fdID);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		firstSeed = input.getInt();
		secondSeed = input.getInt();
		ip = input.getInt();
		fdID = input.getInt();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public int getFirstSeed()
	{
		return firstSeed;
	}

	public void setFirstSeed(int firstSeed)
	{
		this.firstSeed = firstSeed;
	}

	public int getSecondSeed()
	{
		return secondSeed;
	}

	public void setSecondSeed(int secondSeed)
	{
		this.secondSeed = secondSeed;
	}

	public int getIP()
	{
		return ip;
	}

	public void setIP(int ip)
	{
		this.ip = ip;
	}

	public int getFdID()
	{
		return fdID;
	}

	public void setFdID(int fdID)
	{
		this.fdID = fdID;
	}

	@Override
	protected int length()
	{
		return 20;
	}

	@Override
	public String getName()
	{
		return "REQ_AUTH_ACCOUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REQ_AUTH_ACCOUNT;
	}
}

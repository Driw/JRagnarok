package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_AUTH_ACCOUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AuthAccountResult extends RequestPacket
{
	private int accountID;
	private int firstSeed;
	private int secondSeed;
	private boolean result;
	private int requestID;
	private int version;
	private ClientType clientType;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putInt(firstSeed);
		output.putInt(secondSeed);
		output.putByte(b(result ? 1 : 0));
		output.putInt(requestID);
		output.putInt(version);
		output.putByte(clientType.CODE);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		firstSeed = input.getInt();
		secondSeed = input.getInt();
		result = input.getByte() == 1 ? true : false;
		requestID = input.getInt();
		version = input.getInt();
		clientType = ClientType.parse(input.getByte());
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

	public boolean isResult()
	{
		return result;
	}

	public void setResult(boolean result)
	{
		this.result = result;
	}

	public int getRequestID()
	{
		return requestID;
	}

	public void setRequestID(int requestID)
	{
		this.requestID = requestID;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public ClientType getClientType()
	{
		return clientType;
	}

	public void setClientType(ClientType clientType)
	{
		this.clientType = clientType;
	}

	@Override
	protected int length()
	{
		return 22;
	}

	@Override
	public String getName()
	{
		return "RES_AUTH_ACCOUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_RES_AUTH_ACCOUNT;
	}
}

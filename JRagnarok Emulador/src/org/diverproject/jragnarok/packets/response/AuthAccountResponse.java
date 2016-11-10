package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_AUTH_ACCOUNT;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.login.structures.ClientType;
import org.diverproject.util.stream.Output;

public class AuthAccountResponse extends ResponsePacket
{
	public static final byte OK = 0;
	public static final byte FAILED = 1;

	private int accountID;
	private int firstSeed;
	private int secondSeed;
	private byte result;
	private int requestID;
	private int version;
	private ClientType clientType;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putInt(firstSeed);
		output.putInt(secondSeed);
		output.putByte(result);
		output.putInt(requestID);
		output.putInt(version);
		output.putByte(clientType.CODE);
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public void setFirstSeed(int firstSeed)
	{
		this.firstSeed = firstSeed;
	}

	public void setSecondSeed(int secondSeed)
	{
		this.secondSeed = secondSeed;
	}

	public void setResult(byte result)
	{
		this.result = result;
	}

	public void setRequestID(int requestID)
	{
		this.requestID = requestID;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public void setClientType(ClientType clienType)
	{
		this.clientType = clienType;
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

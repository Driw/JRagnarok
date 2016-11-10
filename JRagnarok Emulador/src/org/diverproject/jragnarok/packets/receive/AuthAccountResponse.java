package org.diverproject.jragnarok.packets.receive;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_AUTH_ACCOUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.login.structures.ClientType;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AuthAccountResponse extends RequestPacket
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
		output.putInts(accountID, firstSeed, secondSeed);
		output.putByte(result);
		output.putInts(requestID, version);
		output.putByte(clientType.CODE);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		firstSeed = input.getInt();
		secondSeed = input.getInt();
		result = input.getByte();
		requestID = input.getInt();
		version = input.getInt();
		clientType = ClientType.parse(input.getByte());
	}

	public int getAccountID()
	{
		return accountID;
	}

	public int getFirstSeed()
	{
		return firstSeed;
	}

	public int getSecondSeed()
	{
		return secondSeed;
	}

	public byte getResult()
	{
		return result;
	}

	public int getRequestID()
	{
		return requestID;
	}

	public int getVersion()
	{
		return version;
	}

	public ClientType getClientType()
	{
		return clientType;
	}

	@Override
	public String getName()
	{
		return "AUTH_ACCOUNT_RES";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_RES_AUTH_ACCOUNT;
	}

	@Override
	protected int length()
	{
		return 22;
	}
}

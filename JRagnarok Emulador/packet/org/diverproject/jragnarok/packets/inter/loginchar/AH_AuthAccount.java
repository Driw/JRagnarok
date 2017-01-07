package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_AUTH_ACCOUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_AuthAccount extends RequestPacket
{
	private int fileDescriptorID;
	private int accountID;
	private int firstSeed;
	private int secondSeed;
	private boolean result;
	private int version;
	private ClientType clientType;
	private Sex sex;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(fileDescriptorID);
		output.putInt(accountID);
		output.putInt(firstSeed);
		output.putInt(secondSeed);
		output.putByte(b(result ? 1 : 0));
		output.putInt(version);
		output.putByte(clientType.CODE);
		output.putByte(sex.code());
	}

	@Override
	protected void receiveInput(Input input)
	{
		fileDescriptorID = input.getInt();
		accountID = input.getInt();
		firstSeed = input.getInt();
		secondSeed = input.getInt();
		result = input.getByte() == 1;
		version = input.getInt();
		clientType = ClientType.parse(input.getByte());
		sex = Sex.parse(input.getByte());
	}

	public int getFileDescriptorID()
	{
		return fileDescriptorID;
	}

	public void setFileDescriptorID(int fileDescriptorID)
	{
		this.fileDescriptorID = fileDescriptorID;
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

	public Sex getSex()
	{
		return sex;
	}

	public void setSex(Sex sex)
	{
		this.sex = sex;
	}

	@Override
	public String getName()
	{
		return "AH_AUTH_ACCOUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_AUTH_ACCOUNT;
	}

	@Override
	protected int length()
	{
		return 25;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("fdID", fileDescriptorID);
		description.append("accountID", accountID);
		description.append("seed", format("%d|%d", firstSeed, secondSeed));

		if (result)
		{
			description.append("version", version);
			description.append("clientType", clientType);
		}

		description.append("sex", sex);
	}
}

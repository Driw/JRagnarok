package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_AUTH_ACCOUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_AuthAccount extends RequestPacket
{
	private int accountID;
	private int firstSeed;
	private int secondSeed;
	private int ip;
	private int fileDescriptorID;
	private Sex sex;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putInt(firstSeed);
		output.putInt(secondSeed);
		output.putInt(ip);
		output.putInt(fileDescriptorID);
		output.putByte(sex.code());
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		firstSeed = input.getInt();
		secondSeed = input.getInt();
		ip = input.getInt();
		fileDescriptorID = input.getInt();
		sex = Sex.parse(input.getByte());
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

	public int getFileDescriptorID()
	{
		return fileDescriptorID;
	}

	public void setFileDescriptorID(int fdID)
	{
		this.fileDescriptorID = fdID;
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
	protected int length()
	{
		return 23;
	}

	@Override
	public String getName()
	{
		return "HA_AUTH_ACCOUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_AUTH_ACCOUNT;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("firstSeed", firstSeed);
		description.append("secondSeed", secondSeed);
		description.append("ip", SocketUtil.socketIP(ip));
		description.append("fileDescriptorID", fileDescriptorID);
		description.append("sex", sex);
	}
}

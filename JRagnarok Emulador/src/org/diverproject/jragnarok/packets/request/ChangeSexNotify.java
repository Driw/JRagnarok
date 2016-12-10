package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CHANGE_SEX_NOTIFY;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class ChangeSexNotify extends RequestPacket
{
	private int accountID;
	private char sex;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putChar(sex);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		sex = input.getChar();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public char getSex()
	{
		return sex;
	}

	public void setSex(char sex)
	{
		this.sex = sex;
	}

	@Override
	public String getName()
	{
		return "CHANGE_SEX_NOTIFY";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CHANGE_SEX_NOTIFY;
	}

	@Override
	protected int length()
	{
		return 5;
	}
}

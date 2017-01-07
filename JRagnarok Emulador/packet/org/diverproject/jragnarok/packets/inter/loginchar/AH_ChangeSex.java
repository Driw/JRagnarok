package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_CHANGE_SEX;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_ChangeSex extends RequestPacket
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
		return "AH_CHANGE_SEX";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_CHANGE_SEX;
	}

	@Override
	protected int length()
	{
		return 7;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("sex", sex);
	}
}

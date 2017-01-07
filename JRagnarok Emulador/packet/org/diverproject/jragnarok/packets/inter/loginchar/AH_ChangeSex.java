package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_CHANGE_SEX;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_ChangeSex extends RequestPacket
{
	private int accountID;
	private Sex sex;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putChar(sex.c);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		sex = Sex.parse(input.getChar());
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
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

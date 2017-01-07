package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_CHANGE_EMAIL;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_ChangeEmail extends RequestPacket
{
	private int accountID;
	private String actualEmail;
	private String newEmail;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putString(actualEmail, EMAIL_LENGTH);
		output.putString(newEmail, EMAIL_LENGTH);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		actualEmail = strclr(input.getString(EMAIL_LENGTH));
		newEmail = strclr(input.getString(EMAIL_LENGTH));
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public String getActualEmail()
	{
		return actualEmail;
	}

	public void setActualEmail(String actualEmail)
	{
		this.actualEmail = actualEmail;
	}

	public String getNewEmail()
	{
		return newEmail;
	}

	public void setNewEmail(String newEmail)
	{
		this.newEmail = newEmail;
	}

	@Override
	public String getName()
	{
		return "HA_CHANGE_EMAIL";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_CHANGE_EMAIL;
	}

	@Override
	protected int length()
	{
		return 86;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("actualEmail", actualEmail);
		description.append("newEmail", newEmail);
	}
}

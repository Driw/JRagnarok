package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.PINCODE_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_ACCOUNT_DATA;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AccountDataResult extends RequestPacket
{
	private int fdID;
	private int accountID;
	private String email;
	private int expirationTime;
	private byte groupID;
	private byte charSlots;
	private String birthdate;
	private String pincode;
	private int pincodeChage;
	private boolean vip;
	private byte charVip;
	private byte charBilling;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(fdID);
		output.putInt(accountID);
		output.putString(email, EMAIL_LENGTH);
		output.putInt(expirationTime);
		output.putByte(groupID);
		output.putByte(charSlots);
		output.putString(birthdate, 10);
		output.putString(pincode, PINCODE_LENGTH);
		output.putInt(pincodeChage);
		output.putByte(b(vip ? 1 : 0));
		output.putByte(charVip);
		output.putByte(charBilling);
	}

	@Override
	protected void receiveInput(Input input)
	{
		fdID = input.getInt();
		accountID = input.getInt();
		email = strclr(input.getString(EMAIL_LENGTH));
		expirationTime = input.getInt();
		groupID = input.getByte();
		charSlots = input.getByte();
		birthdate = strclr(input.getString(10));
		pincode = input.getString(PINCODE_LENGTH);
		pincodeChage = input.getInt();
		vip = input.getByte() == 1;
		charVip = input.getByte();
		charBilling = input.getByte();
	}

	public int getFdID()
	{
		return fdID;
	}

	public void setFdID(int fdID)
	{
		this.fdID = fdID;
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = strcap(email, EMAIL_LENGTH);
	}

	public int getExpirationTime()
	{
		return expirationTime;
	}

	public void setExpirationTime(int expirationTime)
	{
		if (expirationTime > 0)
			this.expirationTime = expirationTime;
	}

	public byte getGroupID()
	{
		return groupID;
	}

	public void setGroupID(byte groupID)
	{
		if (groupID > 0)
			this.groupID = groupID;
	}

	public byte getCharSlots()
	{
		return charSlots;
	}

	public void setCharSlots(byte charSlots)
	{
		if (charSlots > 0)
			this.charSlots = charSlots;
	}

	public String getBirthdate()
	{
		return birthdate;
	}

	public void setBirthdate(String birthdate)
	{
		if (birthdate.length() == 10)
			this.birthdate = birthdate;
	}

	public String getPincode()
	{
		return pincode;
	}

	public void setPincode(String pincode)
	{
		if (pincode.length() == PINCODE_LENGTH)
			this.pincode = pincode;
	}

	public int getPincodeChage()
	{
		return pincodeChage;
	}

	public void setPincodeChage(int pincodeChage)
	{
		if (pincodeChage > 0)
			this.pincodeChage = pincodeChage;
	}

	public boolean isVip()
	{
		return vip;
	}

	public void setVip(boolean vip)
	{
		this.vip = vip;
	}

	public byte getCharVip()
	{
		return charVip;
	}

	public void setCharVip(byte charVip)
	{
		if (charVip > 0)
			this.charVip = charVip;
	}

	public byte getCharBilling()
	{
		return charBilling;
	}

	public void setCharBilling(byte charBilling)
	{
		if (charBilling > 0)
			this.charBilling = charBilling;
	}

	@Override
	public String getName()
	{
		return "RES_ACCOUNT_DATA";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_RES_ACCOUNT_DATA;
	}

	@Override
	protected int length()
	{
		return 27 + EMAIL_LENGTH + PINCODE_LENGTH;
	}
}

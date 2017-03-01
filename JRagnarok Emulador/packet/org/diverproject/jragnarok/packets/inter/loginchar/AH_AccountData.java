package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnaork.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnaork.JRagnarokConstants.PINCODE_LENGTH;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_ACCOUNT_DATA;
import static org.diverproject.util.Util.b;
import static org.diverproject.util.Util.strcap;
import static org.diverproject.util.Util.strclr;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_AccountData extends RequestPacket
{
	private int fdID;
	private int accountID;
	private String email;
	private int expirationTime;
	private int groupID;
	private int vipID;
	private byte charSlots;
	private String birthdate;
	private boolean pincodeEnabled;
	private String pincode;
	private long pincodeChage;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(fdID);
		output.putInt(accountID);
		output.putString(email, EMAIL_LENGTH);
		output.putInt(expirationTime);
		output.putInt(groupID);
		output.putInt(vipID);
		output.putByte(charSlots);
		output.putString(birthdate, 10);
		output.putByte(b(pincodeEnabled ? 1 : 0));
		output.putString(pincode, PINCODE_LENGTH);
		output.putLong(pincodeChage);
	}

	@Override
	protected void receiveInput(Input input)
	{
		fdID = input.getInt();
		accountID = input.getInt();
		email = strclr(input.getString(EMAIL_LENGTH));
		expirationTime = input.getInt();
		groupID = input.getInt();
		vipID = input.getInt();
		charSlots = input.getByte();
		birthdate = strclr(input.getString(10));
		pincodeEnabled = input.getByte() == 1;
		pincode = input.getString(PINCODE_LENGTH);
		pincodeChage = input.getLong();
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

	public int getGroupID()
	{
		return groupID;
	}

	public void setGroupID(int groupID)
	{
		if (groupID > 0)
			this.groupID = groupID;
	}

	public int getVipID()
	{
		return vipID;
	}

	public void setVipID(int vipID)
	{
		this.vipID = vipID;
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

	public boolean isPincodeEnabled()
	{
		return pincodeEnabled;
	}

	public void setPincodeEnabled(boolean pincodeEnabled)
	{
		this.pincodeEnabled = pincodeEnabled;
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

	public long getPincodeChage()
	{
		return pincodeChage;
	}

	public void setPincodeChage(long pincodeChage)
	{
		if (pincodeChage > 0)
			this.pincodeChage = pincodeChage;
	}

	@Override
	public String getName()
	{
		return "AH_ACCOUNT_DATA";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_ACCOUNT_DATA;
	}

	@Override
	protected int length()
	{
		return 42 + EMAIL_LENGTH + PINCODE_LENGTH;
	}

	@Override
	public void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("fdID", fdID);
		description.append("accountID", accountID);
		description.append("email", email);
		description.append("expirationTime", expirationTime);
		description.append("groupID", groupID);
		description.append("vipID", vipID);
		description.append("charSlots", charSlots);
		description.append("birthdate", birthdate);

		if (pincodeEnabled)
		{
			description.append("pincode", pincode);
			description.append("pincodeChage", pincodeChage);
		}
	}
}

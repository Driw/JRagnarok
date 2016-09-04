package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.PINCODE_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class Account extends Login
{
	private String email;
	private int group;
	private byte charSlots;
	private AccountState state;
	private Time unban;
	private Time expiration;
	private int loginCount;
	private String lastIP;
	private String birthDate;
	private boolean usePincode;
	private String pincode;
	private Time pincodeChange;
	private int oldGroup;
	private Time vipTime;

	public String getEmail()
	{
		return email;
	}

	public Account()
	{
		group = -1;
		unban = new Time();
		expiration = new Time();
		pincodeChange = new Time();
		vipTime = new Time();
		birthDate = "0000-00-00";
	}

	public void setEmail(String email)
	{
		this.email = strcap(email, EMAIL_LENGTH);
	}

	public int getGroup()
	{
		return group;
	}

	public void setGroup(int group)
	{
		if (group > 0)
			this.group = group;
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

	public AccountState getState()
	{
		return state;
	}

	public void setState(AccountState state)
	{
		if (state != null)
			this.state = state;
	}

	public Time getUnban()
	{
		return unban;
	}

	public Time getExpiration()
	{
		return expiration;
	}

	public int getLoginCount()
	{
		return loginCount;
	}

	public void setLoginCount(int loginCount)
	{
		if (loginCount > 0)
			this.loginCount = loginCount;
	}

	public String getLastIP()
	{
		return lastIP;
	}

	public void setLastIP(String lastIP)
	{
		this.lastIP = strcap(lastIP, 16);
	}

	public void setBirthDate(String birthDate)
	{
		this.birthDate = strcap(birthDate, 10 + 1);
	}

	public String getBirthDate()
	{
		return birthDate;
	}

	public boolean isUsePincode()
	{
		return usePincode;
	}

	public void setUsePincode(boolean usePincode)
	{
		this.usePincode = usePincode;
	}

	public String getPincode()
	{
		return pincode;
	}

	public void setPincode(String pincode)
	{
		this.pincode = strcap(pincode, PINCODE_LENGTH + 1);
	}

	public Time getPincodeChange()
	{
		return pincodeChange;
	}

	public void setPincodeChange(Time pincodeChange)
	{
		this.pincodeChange = pincodeChange;
	}

	public int getOldGroup()
	{
		return oldGroup;
	}

	public void setOldGroup(int oldGroup)
	{
		this.oldGroup = oldGroup;
	}

	public Time getVipTime()
	{
		return vipTime;
	}

	public void setVipTime(Time vipTime)
	{
		this.vipTime = vipTime;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("email", email);
		description.append("group", group);
		description.append("charSlots", charSlots);
		description.append("state", state);
		description.append("unban", unban);
		description.append("expiration", expiration);
		description.append("loginCount", loginCount);
		description.append("lastIP", lastIP);
		description.append("birthDate", birthDate);

		if (usePincode)
		{
			description.append("pincode", pincode);
			description.append("pincodChange", pincodeChange);
		}

		if (oldGroup != -1)
		{
			description.append("oldGroup", oldGroup);
			description.append("vipTime", vipTime);
		}
	}
}

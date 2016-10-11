package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class Account extends Login
{
	private static final String DEFAULT_EMAIL = "a@a.com";
	private static final String DEFAULT_BIRTHDATE = "0000-00-00";

	private String email;
	private String birthDate;
	private byte charSlots;
	private int loginCount;

	private Time unban;
	private Time expiration;
	private Pincode pincode;
	private AccountState state;
	private AccountGroup group;
	private InternetProtocol lastIP;

	public String getEmail()
	{
		return email;
	}

	public Account()
	{
		email = DEFAULT_EMAIL;
		birthDate = DEFAULT_BIRTHDATE;

		unban = new Time();
		expiration = new Time();
		pincode = new Pincode();
		lastIP = new InternetProtocol();
		group = new AccountGroup();

		state = AccountState.EMAIL_CONFIRMATION;
	}

	public void setEmail(String email)
	{
		// TODO : validar e-mail
		this.email = strcap(email, EMAIL_LENGTH);
	}

	public String getBirthDate()
	{
		return birthDate;
	}

	public void setBirthDate(String birthDate)
	{
		// TODO : validar data de aniversário
		this.birthDate = strcap(birthDate, 10);
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

	public int getLoginCount()
	{
		return loginCount;
	}

	public void setLoginCount(int loginCount)
	{
		if (loginCount > 0)
			this.loginCount = loginCount;
	}

	public Time getUnban()
	{
		return unban;
	}

	public Time getExpiration()
	{
		return expiration;
	}

	public Pincode getPincode()
	{
		return pincode;
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

	public AccountGroup getGroup()
	{
		return group;
	}

	public void setGroup(AccountGroup group)
	{
		this.group = group;
	}

	public InternetProtocol getLastIP()
	{
		return lastIP;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("email", email);
		description.append("birthDate", birthDate);
		description.append("charSlots", charSlots);
		description.append("loginCount", loginCount);
		description.append("state", state);
		description.append("lastIP", lastIP.getString());

		if (group != null)
			description.append("group", group.getCurrentGroup().getName());

		if (unban.get() != 0)
			description.append("unban", unban);

		if (expiration.get() != 0)
			description.append("expiration", expiration);

		if (pincode.isEnabled())
		{
			description.append("pincode", pincode.getCode());
			description.append("pincodChange", pincode.getChanged());
		}
	}
}

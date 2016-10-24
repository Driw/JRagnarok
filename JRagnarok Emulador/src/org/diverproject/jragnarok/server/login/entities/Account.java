package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class Account
{
	private static final String DEFAULT_EMAIL = "a@a.com";
	private static final String DEFAULT_BIRTHDATE = "0000-00-00";

	private Login login;
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

	public Account()
	{
		email = DEFAULT_EMAIL;
		birthDate = DEFAULT_BIRTHDATE;

		login = new Login();
		unban = new Time();
		expiration = new Time();
		lastIP = new InternetProtocol();
		pincode = new Pincode();
		group = new AccountGroup();

		state = AccountState.NONE;
	}

	public Login getLogin()
	{
		return login;
	}

	public void setLogin(Login login)
	{
		if (login != null)
			this.login = login;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		// TODO : validar e-mail

		if (email != null)
			this.email = strcap(email, EMAIL_LENGTH);
	}

	public String getBirthDate()
	{
		return birthDate;
	}

	public void setBirthDate(String birthDate)
	{
		// TODO : validar data de aniversário

		if (birthDate != null)
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

	public void setPincode(Pincode pincode)
	{
		if (pincode != null)
			this.pincode = pincode;
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
		if (group != null)
			this.group = group;
	}

	public InternetProtocol getLastIP()
	{
		return lastIP;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		if (login != null)
			login.toString(description);

		description.append("email", email);
		description.append("birthDate", birthDate);
		description.append("charSlots", charSlots);
		description.append("loginCount", loginCount);
		description.append("state", state);
		description.append("lastIP", lastIP.getString());

		if (group != null && group.getCurrentGroup() != null)
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

		return description.toString();
	}
}

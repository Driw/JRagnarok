package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.util.lang.IntUtil.interval;
import static org.diverproject.jragnarok.JRagnarokConstants.EMAIL_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.util.Util.strcap;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class Account implements Login
{
	private static final String DEFAULT_EMAIL = "a@a.com";
	private static final String DEFAULT_BIRTHDATE = "0000-00-00";

	private int id;
	private String username;
	private String password;
	private Sex sex;
	private Time lastLogin;
	private Time registered;
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

		lastLogin = new Time();
		registered = new Time();
		expiration = new Time();
		unban = new Time();
		lastIP = new InternetProtocol();
		pincode = new Pincode();
		group = new AccountGroup();

		state = AccountState.NONE;
	}

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public Sex getSex()
	{
		return sex;
	}

	public void setSex(Sex sex)
	{
		this.sex = sex;
	}

	public Time getLastLogin()
	{
		return lastLogin;
	}

	public Time getRegistered()
	{
		return registered;
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

	public void setCharSlots(byte charSlosts)
	{
		if (interval(charSlosts, 0, MAX_CHARS))
			this.charSlots = charSlosts;
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

	public InternetProtocol getLastIP()
	{
		return lastIP;
	}

	public int getGroupID()
	{
		return getGroup().getCurrentGroup().getID();
	}

	public int getAccessLevel()
	{
		return getGroup().getCurrentGroup().getAccessLevel();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("username", username);
		description.append("password", password);
		description.append("registered", registered);
		description.append("lastLogin", lastLogin);
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

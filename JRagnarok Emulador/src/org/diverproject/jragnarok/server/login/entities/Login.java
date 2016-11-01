package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokConstants.USERNAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.PASSWORD_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class Login
{
	private int id;
	private String username;
	private String password;
	private Time lastLogin;
	private Time registered;

	public Login()
	{
		lastLogin = new Time(System.currentTimeMillis());
		registered = new Time(System.currentTimeMillis());
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
		this.username = strcap(username, USERNAME_LENGTH);
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = strcap(password, PASSWORD_LENGTH);
	}

	public Time getLastLogin()
	{
		return lastLogin;
	}

	public Time getRegistered()
	{
		return registered;
	}
	
	protected void toString(ObjectDescription description)
	{
		description.append("id", id);
		description.append("username", username);
		description.append("password", password);
		description.append("lastLogin", lastLogin);		
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		toString(description);

		return description.toString();
	}
}

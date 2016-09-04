package org.diverproject.jragnarok.server.login.structures;

import static org.diverproject.jragnarok.JRagnarokConstants.NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.PASSWORD_LENGTH;
import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.util.ObjectDescription;

public class Login
{
	private int id;
	private String username;
	private String password;
	private Sex sex;
	private String lastLogin;

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
		this.username = strcap(username, NAME_LENGTH);
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = strcap(password, PASSWORD_LENGTH);
	}

	public Sex getSex()
	{
		return sex;
	}

	public void setSex(Sex sex)
	{
		if (sex != null)
			this.sex = sex;
	}

	public String getLastLogin()
	{
		return lastLogin;
	}

	public void setLastLogin(String lastLogin)
	{
		this.lastLogin = lastLogin;
	}

	protected void toString(ObjectDescription description)
	{
		description.append("id", id);
		description.append("username", username);
		description.append("password", password);
		description.append("sex", sex);
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

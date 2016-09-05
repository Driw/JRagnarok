package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class LoginLog
{
	private Time time;
	private InternetProtocol ip;
	private String user;
	private int rCode;
	private String message;

	public LoginLog()
	{
		time = new Time();
		ip = new InternetProtocol();
	}

	public Time getTime()
	{
		return time;
	}

	public InternetProtocol getIP()
	{
		return ip;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = strcap(user, 23);
	}

	public int getRCode()
	{
		return rCode;
	}

	public void setRCode(int rCode)
	{
		this.rCode = rCode;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String log)
	{
		this.message = strcap(log, 255);
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("time", time);
		description.append("ip", ip.getString());
		description.append("user", user);
		description.append("rCode", rCode);
		description.append("message", message);

		return description.toString();
	}
}

package org.diverproject.jragnarok.server.login.entities;

import org.diverproject.util.Time;

public interface Login
{
	public int getID();
	public String getUsername();
	public String getPassword();
	public Time getLastLogin();
	public Time getRegistered();
}

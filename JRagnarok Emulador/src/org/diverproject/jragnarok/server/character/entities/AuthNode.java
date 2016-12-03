package org.diverproject.jragnarok.server.character.entities;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.login.entities.Group;
import org.diverproject.util.Time;

public class AuthNode
{
	private int accountID;
	private int charID;
	private LoginSeed seed;
	private InternetProtocol ip;
	private Time expiration;
	private Group group;
	private int changingMapServers;
	private int version;

	public AuthNode()
	{
		changingMapServers = 1;

		expiration = new Time();
		ip = new InternetProtocol();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public int getCharID()
	{
		return charID;
	}

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	public LoginSeed getSeed()
	{
		return seed;
	}

	public void setSeed(LoginSeed seed)
	{
		this.seed = seed;
	}

	public InternetProtocol getIP()
	{
		return ip;
	}

	public Time getExpiration()
	{
		return expiration;
	}

	public Group getGroup()
	{
		return group;
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	public int getChangingMapServers()
	{
		return changingMapServers;
	}

	public void setChangingMapServers(int changingMapServers)
	{
		this.changingMapServers = changingMapServers;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}
}

package org.diverproject.jragnarok.server.login.entities;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.util.ObjectDescription;

public class AuthNode
{
	private int accountID;
	private LoginSeed seed;
	private InternetProtocol ip;
	private int version;
	private ClientType clientType;

	public AuthNode()
	{
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

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public ClientType getClientType()
	{
		return clientType;
	}

	public void setClientType(ClientType clientType)
	{
		this.clientType = clientType;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);
		description.append("seed", format("%d|%d", seed.getFirst(), seed.getSecond()));
		description.append("ip", ip != null ? ip.getString() : null);
		description.append("version", version);
		description.append("clientType", clientType);

		return description.toString();
	}
}

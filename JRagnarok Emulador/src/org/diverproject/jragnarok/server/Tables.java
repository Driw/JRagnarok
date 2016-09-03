package org.diverproject.jragnarok.server;

public class Tables
{
	private static final Tables INSTANCE = new Tables();

	public static Tables getInstance()
	{
		return INSTANCE;
	}

	private String loginLog;

	private Tables()
	{
		
	}

	public String getLoginLog()
	{
		return loginLog;
	}

	public void setLoginLog(String loginLog)
	{
		this.loginLog = loginLog;
	}
}

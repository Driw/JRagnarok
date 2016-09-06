package org.diverproject.jragnarok.server;

public class Tables
{
	private static final Tables INSTANCE = new Tables();

	public static Tables getInstance()
	{
		return INSTANCE;
	}

	private String login;
	private String loginLog;

	private Tables()
	{
		login = "login";
		loginLog = "login_log";
	}

	public String getLogin()
	{
		return login;
	}

	public void setLogin(String login)
	{
		this.login = login;
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

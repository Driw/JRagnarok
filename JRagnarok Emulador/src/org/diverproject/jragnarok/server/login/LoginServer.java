package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHAR_BILLING;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHAR_VIP;
import static org.diverproject.util.MessageUtil.die;

import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.config.ConfigClient;
import org.diverproject.jragnarok.server.config.ConfigIpBan;
import org.diverproject.jragnarok.server.config.ConfigLog;
import org.diverproject.jragnarok.server.config.ConfigLogin;

public class LoginServer extends Server implements ServerListener
{
	private static final String HOST = "localhost";
	private static final int PORT = 6900;

	private static final LoginServer INSTANCE;

	static
	{
		LoginServer instance = null;

		try {
			instance = new LoginServer();
		} catch (RagnarokException e) {
			die(e);
		}

		INSTANCE = instance;
	}

	public LoginServer() throws RagnarokException
	{
		super(PORT);

		setListener(this);
	}

	@Override
	protected LoginConfig setServerConfig()
	{
		return new LoginConfig();
	}

	@Override
	public void onCreate() throws RagnarokException
	{
		setDefaultConfigs();
	}

	private void setDefaultConfigs()
	{
		ConfigLogin.getIp().setValue(new InternetProtocol());
		ConfigLogin.getPort().setValue(6900);
		ConfigLogin.getIpSyncinterval().setValue(0);
		ConfigLogin.getDateformat().setValue("YY-mm-dd HH:MM:SS");
		ConfigLogin.getConsole().setValue(true);
		ConfigLogin.getNewAccountFlag().setValue(true);
		ConfigLogin.getNewAccountLengthLimit().setValue(true);
		ConfigLogin.getUseMD5password().setValue(false);
		ConfigLogin.getGroupToConnect().setValue(-1);
		ConfigLogin.getMinGroup().setValue(-1);
		ConfigLogin.getAllowedRegs().setValue(1);
		ConfigLogin.getTimeAllowed().setValue(10);

		ConfigIpBan.getEnableService().setValue(true);
		ConfigIpBan.getCleanupinterval().setValue(60);
		ConfigIpBan.getPassFailureInterval().setValue(5);
		ConfigIpBan.getPassFailureLimit().setValue(7);
		ConfigIpBan.getPassFailureDuration().setValue(5);

		ConfigLog.getLogLogin().setValue(true);

		ConfigClient.getCheckVersion().setValue(false);
		ConfigClient.getVersion().setValue(50); // TODO date2version(PACKETVER)
		ConfigClient.getHashCheck().setValue(0);
		ConfigClient.getHashNodes().setValue(null);
		ConfigClient.getCharPerAccount().setValue(MAX_CHARS - MAX_CHAR_VIP - MAX_CHAR_BILLING);
	}

	@Override
	public void onCreated() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRunning() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStoped() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroyed() throws RagnarokException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getThreadName()
	{
		return "Servidor de Acesso";
	}

	@Override
	protected int getThreadPriority()
	{
		return Thread.MIN_PRIORITY;
	}

	@Override
	protected String getAddress()
	{
		return HOST;
	}

	@Override
	protected void dispatchSocket(Socket socket)
	{
		// TODO Auto-generated method stub
		
	}

	public static LoginServer getInstance()
	{
		return INSTANCE;
	}
}

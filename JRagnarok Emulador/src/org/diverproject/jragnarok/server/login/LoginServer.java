package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHAR_BILLING;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHAR_VIP;
import static org.diverproject.jragnarok.JRagnarokUtil.minutes;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.util.MessageUtil.die;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.ConfigLoad;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorAction;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.config.ConfigClient;
import org.diverproject.jragnarok.server.config.ConfigFiles;
import org.diverproject.jragnarok.server.config.ConfigIpBan;
import org.diverproject.jragnarok.server.config.ConfigLog;
import org.diverproject.jragnarok.server.config.ConfigLogin;
import org.diverproject.jragnarok.server.config.ConfigSQL;
import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.jragnarok.server.login.services.LoginCharacterService;
import org.diverproject.jragnarok.server.login.services.LoginClientService;
import org.diverproject.jragnarok.server.login.services.LoginIpBanService;
import org.diverproject.jragnarok.server.login.services.LoginLogService;
import org.diverproject.jragnarok.server.login.services.LoginService;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;

public class LoginServer extends Server implements ServerListener
{
	private static final LoginServer INSTANCE;

	private LoginCharServers charServers;
	private LoginLogService logService;
	private LoginClientService clientService;
	private LoginCharacterService charService;
	private LoginIpBanService ipBanService;
	private LoginService loginService;

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
		setListener(this);
		charServers = new LoginCharServers();
	}

	public LoginCharServers getCharServers()
	{
		return charServers;
	}

	public LoginLogService getLogService()
	{
		return logService;
	}

	public LoginClientService getClientService()
	{
		return clientService;
	}

	public LoginCharacterService getCharService()
	{
		return charService; 
	}

	public LoginIpBanService getIpBanService()
	{
		return ipBanService;
	}

	public LoginService getLoginService()
	{
		return loginService;
	}

	@Override
	public void onCreate() throws RagnarokException
	{
		setDefaultConfigs();
		readConfigFiles();
	}

	private void setDefaultConfigs()
	{
		ConfigFiles.getSystemConfig().setValue("config/System.conf");
		ConfigFiles.getSqlConnectionConfig().setValue("config/SqlConnection.conf");
		ConfigFiles.getLoginConfig().setValue("config/Login.conf");
		ConfigFiles.getIpBanConfig().setValue("config/IpBan.conf");
		ConfigFiles.getLogConfig().setValue("config/Log.conf");
		ConfigFiles.getClientConfig().setValue("config/Client.conf");

		ConfigSQL.getHost().setValue("localhost");
		ConfigSQL.getUsername().setValue("jragnarok");
		ConfigSQL.getPassword().setValue("jragnarok");
		ConfigSQL.getDatabase().setValue("jragnarok");
		ConfigSQL.getPort().setValue(3306);
		ConfigSQL.getLegacyDatetime().setValue(false);

		ConfigLogin.getIp().setValue(new InternetProtocol());
		ConfigLogin.getPort().setValue(6900);
		ConfigLogin.getUsername().setValue("server");
		ConfigLogin.getPassword().setValue("jragnarok");
		ConfigLogin.getIpSyncInterval().setValue(0);
		ConfigLogin.getDateFormat().setValue("YY-mm-dd HH:MM:SS");
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
		ConfigIpBan.getPassFailureEnable().setValue(true);
		ConfigIpBan.getPassFailureInterval().setValue(5);
		ConfigIpBan.getPassFailureLimit().setValue(7);
		ConfigIpBan.getPassFailureDuration().setValue(5);

		ConfigLog.getLogLogin().setValue(true);

		ConfigClient.getCheckVersion().setValue(false);
		ConfigClient.getVersion().setValue(50); // TODO date2version(PACKETVER)
		ConfigClient.getHashCheck().setValue(0);
		ConfigClient.getHashNodes().setValue(null);
		ConfigClient.getCharPerAccount().setValue(MAX_CHARS - MAX_CHAR_VIP - MAX_CHAR_BILLING);

		setServerConfig(new LoginConfig());
	}

	private void readConfigFiles()
	{
		ConfigLoad load = new ConfigLoad();
		load.setConfigurations(getConfigs().getMap());

		String fileKeys[] = new String[]
		{
			ConfigFiles.getSystemConfig().getName(),
			ConfigFiles.getSqlConnectionConfig().getName(),
			ConfigFiles.getLoginConfig().getName(),
			ConfigFiles.getIpBanConfig().getName(),
			ConfigFiles.getLogConfig().getName(),
			ConfigFiles.getClientConfig().getName(),
		};

		for (String fileKey : fileKeys)
		{
			String filepath = getConfigs().getString(fileKey);

			try {

				load.setFilePath(filepath);
				load.read();

			} catch (RagnarokException e) {
				logError("falha durante a leitura de '%s' (config: %s).\n", filepath, fileKey);
				logExeception(e);
			}
		}
	}

	@Override
	public void onCreated() throws RagnarokException
	{
		logService = new LoginLogService(this);
		clientService = new LoginClientService(this);
		charService = new LoginCharacterService(this);
		ipBanService = new LoginIpBanService(this);
		loginService = new LoginService(this);

		charService.init();
		clientService.init();

		if (getConfigs().getBool("log.login"))
			logService.init();

		if (getConfigs().getBool("ipban.enabled"))
			ipBanService.init();

		setDefaultParser(clientService.parse);

		TimerSystem ts = TimerSystem.getInstance();
		TimerMap timers = ts.getTimers();

		Timer odcTimer = timers.acquireTimer();
		odcTimer.setTick(ts.getLastTick() + minutes(10));
		odcTimer.setListener(onlineDataCleanup);
		ts.getTimers().addInterval(odcTimer, minutes(10));
	}

	@Override
	public void onRunning() throws RagnarokException
	{
		logInfo("o servidor de acesso está pronto (porta: %d).\n", getPort());

		Login login = new Login();
		login.setUsername(getConfigs().getString("login.username"));
		login.setPassword(getConfigs().getString("login.password"));

		// TODO confirmar usuário e senha

		logService.addLoginLog(SocketUtil.socketIPInt(getAddress()), login, 100, "login server started");
	}

	@Override
	public void onStop() throws RagnarokException
	{

	}

	@Override
	public void onStoped() throws RagnarokException
	{
		
	}

	@Override
	public void onDestroy() throws RagnarokException
	{
		
	}

	@Override
	public void onDestroyed() throws RagnarokException
	{
		charService.shutdown();
		FileDescriptor.execute(onDestroyed);
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
		return ((InternetProtocol) getConfigs().getObject("login.ip")).getString();
	}

	@Override
	protected int getPort()
	{
		return getConfigs().getInt("login.port");
	}

	private final TimerListener onlineDataCleanup = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int tick)
		{
			// TODO
		}

		@Override
		public String getName()
		{
			return "onlineDataCleanup";
		};

		@Override
		public String toString()
		{
			ObjectDescription description = new ObjectDescription(getClass());

			description.append(getName());

			return description.toString();
		}
	};

	private final FileDescriptorAction onDestroyed = new FileDescriptorAction()
	{
		@Override
		public void execute(FileDescriptor fd)
		{
			// TODO usar algum pacote que avise do desligamento do servidor

			fd.close();
		}

		@Override
		public String toString()
		{
			ObjectDescription description = new ObjectDescription(getClass());

			description.append("onDestroyed");

			return description.toString();
		}
	};

	public static LoginServer getInstance()
	{
		return INSTANCE;
	}
}

package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHAR_BILLING;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHAR_VIP;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_SERVERS;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.util.MessageUtil.die;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.ConfigLoad;
import org.diverproject.jragnarok.server.ClientCharServer;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.config.ConfigClient;
import org.diverproject.jragnarok.server.config.ConfigFiles;
import org.diverproject.jragnarok.server.config.ConfigIpBan;
import org.diverproject.jragnarok.server.config.ConfigLog;
import org.diverproject.jragnarok.server.config.ConfigLogin;
import org.diverproject.jragnarok.server.login.services.LoginCharacterService;
import org.diverproject.jragnarok.server.login.services.LoginClientService;
import org.diverproject.jragnarok.server.login.services.LoginIpBanService;
import org.diverproject.jragnarok.server.login.services.LoginLogService;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.LoopList;

public class LoginServer extends Server implements ServerListener
{
	private static final LoginServer INSTANCE;

	private List<ClientCharServer> charServers;
	private LoginLogService logService;
	private LoginClientService clientService;
	private LoginCharacterService charService;
	private LoginIpBanService ipBanService;

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

		charServers = new LoopList<>(MAX_SERVERS);

		logService = new LoginLogService(this);
		clientService = new LoginClientService(this);
		charService = new LoginCharacterService(this);
		ipBanService = new LoginIpBanService(this);
	}

	public List<ClientCharServer> getCharServers()
	{
		return charServers;
	}

	@Override
	protected LoginConfig setServerConfig()
	{
		return new LoginConfig();
	}

	private TimerListener waitingDisconnectTimer = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, long tick)
		{
			
		}
	};

	private TimerListener onlineDataCleanup = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, long tick)
		{
			
		}
	};

	@Override
	public void onCreate() throws RagnarokException
	{
		setDefaultConfigs();
		readConfigFiles();
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

		ConfigFiles.getLoginConfig().setValue("config/Login.conf");
		ConfigFiles.getIpBanConfig().setValue("config/IpBan.conf");
		ConfigFiles.getLogConfig().setValue("config/Log.conf");
		ConfigFiles.getClientConfig().setValue("config/Client.conf");
	}

	private void readConfigFiles()
	{
		String fileKey = "";

		ConfigLoad load = new ConfigLoad();
		load.setConfigurations(getConfigs().getMap());

		try {

			fileKey = ConfigFiles.getLoginConfig().getName();
			load.setFilePath(ConfigFiles.getLoginConfig().getValue());
			load.read();

			fileKey = ConfigFiles.getIpBanConfig().getName();
			load.setFilePath(ConfigFiles.getIpBanConfig().getValue());
			load.read();

			fileKey = ConfigFiles.getLogConfig().getName();
			load.setFilePath(ConfigFiles.getLogConfig().getValue());
			load.read();

			fileKey = ConfigFiles.getClientConfig().getName();
			load.setFilePath(ConfigFiles.getClientConfig().getValue());
			load.read();

		} catch (RagnarokException e) {
			logError("falha durante a leitura de %s.\n", fileKey);
			logExeception(e);
		}
	}

	@Override
	public void onCreated() throws RagnarokException
	{
		if (getConfigs().getBool("log.login"))
			logService.init();

		if (getConfigs().getBool("ipban.enabled"))
			ipBanService.init();

		TimerSystem ts = TimerSystem.getInstance();
		ts.addListener(waitingDisconnectTimer, "waitingDisconnectTimer");

		setDefaultParser(clientService.parse);

		ts.addListener(onlineDataCleanup, "onlineDataCleanup");
		ts.addTimerInterval(ts.tick() + 600*1000, onlineDataCleanup, 0, 0, 600*100);
	}

	@Override
	public void onRunning() throws RagnarokException
	{
		logInfo("o servidor de acesso está pronto (porta: %d).\n", getPort());

		logService.log(new InternetProtocol(), "login server", 100, "login server started");
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
		//ClientSystem.flushBuffers();		
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
		return ((InternetProtocol) getConfigs().getObject("login.ip")).get();
	}

	@Override
	protected int getPort()
	{
		return getConfigs().getInt("login.port");
	}

	public static LoginServer getInstance()
	{
		return INSTANCE;
	}
}

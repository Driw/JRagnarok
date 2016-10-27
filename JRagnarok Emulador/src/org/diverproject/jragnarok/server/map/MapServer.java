package org.diverproject.jragnarok.server.map;

import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.util.MessageUtil.die;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.ConfigLoad;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.config.ConfigFiles;
import org.diverproject.jragnarok.server.config.ConfigSQL;

public class MapServer extends Server implements ServerListener
{
	private static final String HOST = "localhost";
	private static final int PORT = 5121;

	private static final MapServer INSTANCE;

	static
	{
		MapServer instance = null;

		try {
			instance = new MapServer();
		} catch (RagnarokException e) {
			die(e);
		}

		INSTANCE = instance;
	}

	public MapServer() throws RagnarokException
	{
		setListener(this);
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

		setServerConfig(new MapConfig());
	}

	private void readConfigFiles()
	{
		ConfigLoad load = new ConfigLoad();
		load.setConfigurations(getConfigs().getMap());

		String fileKeys[] = new String[]
		{
			ConfigFiles.getSystemConfig().getName(),
			ConfigFiles.getSqlConnectionConfig().getName(),
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRunning() throws RagnarokException
	{
		logInfo("o servidor de mapas está pronto (porta: %d).\n", getPort());		
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
		return "Servidor de Mapas";
	}

	@Override
	protected int getThreadPriority()
	{
		return Thread.MAX_PRIORITY;
	}

	@Override
	protected String getAddress()
	{
		return HOST;
	}

	@Override
	protected int getPort()
	{
		return PORT;
	}

	public static MapServer getInstance()
	{
		return INSTANCE;
	}
}

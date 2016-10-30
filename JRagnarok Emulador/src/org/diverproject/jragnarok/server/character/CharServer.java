package org.diverproject.jragnarok.server.character;

import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.util.MessageUtil.die;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.ConfigRead;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.config.ConfigChar;
import org.diverproject.jragnarok.server.config.ConfigFiles;
import org.diverproject.jragnarok.server.config.ConfigLogin;
import org.diverproject.jragnarok.server.config.ConfigSQL;

public class CharServer extends Server implements ServerListener
{
	private static final CharServer INSTANCE;

	static
	{
		CharServer instance = null;

		try {
			instance = new CharServer();
		} catch (RagnarokException e) {
			die(e);
		}

		INSTANCE = instance;
	}

	public CharServer() throws RagnarokException
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
		ConfigFiles.getCharConfig().setValue("config/Character.conf");

		ConfigSQL.getHost().setValue("localhost");
		ConfigSQL.getUsername().setValue("jragnarok");
		ConfigSQL.getPassword().setValue("jragnarok");
		ConfigSQL.getDatabase().setValue("jragnarok");
		ConfigSQL.getPort().setValue(3306);
		ConfigSQL.getLegacyDatetime().setValue(false);

		ConfigLogin.getIp().setValue(new InternetProtocol());
		ConfigLogin.getPort().setValue(6900);

		ConfigChar.getIp().setValue(new InternetProtocol());
		ConfigChar.getPort().setValue(6121);
		ConfigChar.getName().setValue("JRagnarok");
		ConfigChar.getUsername().setValue("jragnarok");
		ConfigChar.getPassword().setValue("jragnarok");
		ConfigChar.getMaintance().setValue(0);
		ConfigChar.getNewDisplay().setValue(0);

		setServerConfig(new CharConfig());
	}

	private void readConfigFiles()
	{
		ConfigRead load = new ConfigRead();
		load.setConfigurations(getConfigs());

		String fileKeys[] = new String[]
		{
			ConfigFiles.getSystemConfig().getName(),
			ConfigFiles.getSqlConnectionConfig().getName(),
			ConfigFiles.getLoginConfig().getName(),
			ConfigFiles.getCharConfig().getName(),
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
		logInfo("o servidor de personagens está pronto (porta: %d).\n", getPort());
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

	public static CharServer getInstance()
	{
		return INSTANCE;
	}
}

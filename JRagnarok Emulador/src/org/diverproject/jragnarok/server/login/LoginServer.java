package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOG_LOGIN;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newClientConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newFileConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newIPBanConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newLogConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newLoginServerConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newSqlConnectionConfigs;
import static org.diverproject.jragnarok.JRagnarokUtil.minutes;
import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logInfo;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.ConfigReader;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorAction;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.jragnarok.server.login.services.LoginCharacterService;
import org.diverproject.jragnarok.server.login.services.LoginClientService;
import org.diverproject.jragnarok.server.login.services.LoginIpBanService;
import org.diverproject.jragnarok.server.login.services.LoginLogService;
import org.diverproject.jragnarok.server.login.services.LoginService;
import org.diverproject.util.SocketUtil;

/**
 * <h1>Servidor de Acesso</h1>
 *
 * <p>Um servidor de acesso � um dos tipos de micro servidores que o JRgarnok disponibiliza.
 * Para esse micro servidor fica sendo de responsabilidade receber a conex�o inicial dos jogadores.
 * Quando um jogador tentar se conectar com o servidor, esse micro servidor � quem dever� receb�-los.</p>
 *
 * <p>No servidor de acesso deve ser recebido as informa��es de acesso da conta (usu�rio e senha),
 * podendo ser seguido de algumas outras informa��es como tipo de senha (md5) e vers�o do cliente.
 * Fica sendo ainda de responsabilidade desse micro servidor informar ao jogador falha de acesso.
 * A falha do acesso poder� ocorrer por usu�rio ou senha inv�lida como vers�o de cliente e afins.</p>
 *
 * <p>Ap�s receber as informa��es de acesso do jogador, dever� validar o acesso (usu�rio e senha),
 * tal como possibilidade de uso de client hash ou senhas criptografados em MD5 (hash).
 * Quando validado dever� enviar ao jogador a lista de servidores de personagens dispon�veis.</p>
 *
 * @see Server
 * @see ServerListener
 * @see LoginCharServers
 * @see LoginCharacterService
 * @see LoginClientService
 * @see LoginIpBanService
 * @see LoginLogService
 * @see LoginService
 *
 * @author Andrew Mello
 */

public class LoginServer extends Server
{
	/**
	 * Lista dos Servidores de Personagens dispon�veis.
	 */
	private LoginCharServers charServers;

	/**
	 * Servi�o para comunica��o com o servidor de personagens.
	 */
	private LoginCharacterService charService;

	/**
	 * Servi�o para comunica��o entre o servidor e o cliente.
	 */
	private LoginClientService clientService;

	/**
	 * Servi�o para banimento de acessos por endere�o de IP.
	 */
	private LoginIpBanService ipBanService;

	/**
	 * Servi�o para registro de acessos.
	 */
	private LoginLogService logService;

	/**
	 * Servi�o para para acesso de contas (servi�o principal)
	 */
	private LoginService loginService;

	/**
	 * Cria um novo micro servidor para receber os novos acessos de clientes.
	 * Define ainda o listener para executar opera��es durante mudan�as de estado.
	 */

	public LoginServer()
	{
		setListener(listener);
	}

	/**
	 * @return aquisi��o dos servidores de personagens dispon�veis para acesso.
	 */

	public LoginCharServers getCharServers()
	{
		return charServers;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o com o servidor de personagens.
	 */

	public LoginCharacterService getCharService()
	{
		return charService;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o do servidor com o cliente.
	 */

	public LoginClientService getClientService()
	{
		return clientService;
	}

	/**
	 * @return aquisi��o do servi�o para banimento de acessos por endere�o de IP.
	 */

	public LoginIpBanService getIpBanService()
	{
		return ipBanService;
	}

	/**
	 * @return aquisi��o do servi�o para registro de acessos no servidor.
	 */

	public LoginLogService getLogService()
	{
		return logService;
	}

	/**
	 * @return aquisi��o do servi�o para acesso de contas (servi�o principal)
	 */

	public LoginService getLoginService()
	{
		return loginService;
	}

	/**
	 * Listener que implementa os m�todos para altera��o de estado do servidor.
	 */

	private final ServerListener listener = new ServerListener()
	{
		@Override
		public void onCreate() throws RagnarokException
		{
			setDefaultConfigs();
			readConfigFiles();
		}

		/**
		 * M�todo que dever� criar as configura��es necess�rias para funcionamento do servidor.
		 * Por padr�o do sistema inicializa as configura��es com seus valores padr�es.
		 * Ap�s isso dever� vincular as configura��es carregadas as configura��es do servidor.
		 * @see JRagnarokConfigs
		 */

		private void setDefaultConfigs()
		{
			Configurations files = newFileConfigs();
			Configurations sqlConnection = newSqlConnectionConfigs();
			Configurations log = newLogConfigs();
			Configurations ipban = newIPBanConfigs();
			Configurations client = newClientConfigs();
			Configurations server = newLoginServerConfigs();
	
			Configurations configs = new Configurations();
			configs.add(files);
			configs.add(sqlConnection);
			configs.add(log);
			configs.add(ipban);
			configs.add(client);
			configs.add(server);
	
			setServerConfig(configs);
		}
	
		/**
		 * Faz a leitura todos os arquivos de configura��es padr�es e dos arquivos individuais do servidor.
		 * A leitura dos arquivos ir� atualizar o valor das configura��es que at� ent�o eram os padr�es.
		 * Configura��es lidas dos arquivos individuais do servidor sobrescrevem as dos arquivos padr�es.
		 */

		private void readConfigFiles()
		{
			Configurations configs = getConfigs();

			ConfigReader reader = new ConfigReader();
			reader.setConfigurations(configs);

			String defaultFolder = configs.getString(SYSTEM_SERVER_DEFAULT_FOLDER);
			String defaultFolderPath = String.format("config/%s", defaultFolder);

			String defaultFilesConfig = configs.getString(SYSTEM_SERVER_DEFAULT_FILES);
			String defaultFiles[] = defaultFilesConfig.split(",");

			String folder = configs.getString(SERVER_FOLDER);
			String folderPath = String.format("config/%s%s", defaultFolder, folder);

			String filesConfig = configs.getString(SERVER_FILES);
			String files[] = filesConfig.split(",");

			readConfigFilesOf(reader, configs, defaultFolderPath, defaultFiles);
			readConfigFilesOf(reader, configs, folderPath, files);
		}

		/**
		 * Realiza a leitura de diversos arquivos em uma determinada pasta especificada.
		 * @param reader objeto que ir� efetuar a leitura das configura��es necess�rias.
		 * @param configs objeto que est� agrupando as configura��es do servidor.
		 * @param folderPath caminho parcial ou completo da pasta que cont�m os arquivos.
		 * @param files vetor contendo o nome de todos os arquivos a serem lidos.
		 */

		private void readConfigFilesOf(ConfigReader reader, Configurations configs, String folderPath, String files[])
		{
			for (String file : files)
			{
				String filepath = String.format("%s%s", folderPath, file);

				try {

					reader.setFilePath(filepath);
					reader.read();

				} catch (RagnarokException e) {
					logError("falha durante a leitura de '%s' (%s:%d).\n", filepath, nameOf(this), getID());
					logExeception(e);
				}
			}
		}

		@Override
		public void onCreated() throws RagnarokException
		{
			charService = new LoginCharacterService(LoginServer.this);
			clientService = new LoginClientService(LoginServer.this);
			ipBanService = new LoginIpBanService(LoginServer.this);
			logService = new LoginLogService(LoginServer.this);
			loginService = new LoginService(LoginServer.this);

			charService.init();
			clientService.init();

			if (getConfigs().getBool(LOG_LOGIN))
				logService.init();

			if (getConfigs().getBool(IPBAN_ENABLED))
				ipBanService.init();

			setDefaultParser(clientService.parse);

			TimerSystem ts = getTimerSystem();
			TimerMap timers = ts.getTimers();

			Timer odcTimer = timers.acquireTimer();
			odcTimer.setTick(ts.getLastTick() + minutes(10));
			odcTimer.setListener(onlineDataCleanup);
			ts.getTimers().addInterval(odcTimer, minutes(10));
		}

		@Override
		public void onRunning() throws RagnarokException
		{
			logInfo("o servidor de acesso est� pronto (porta: %d).\n", getPort());

			Login login = new Login();
			login.setUsername(getConfigs().getString(LOGIN_USERNAME));
			login.setPassword(getConfigs().getString(LOGIN_PASSWORD));

			// TODO confirmar usu�rio e senha

			logService.addLoginLog(SocketUtil.socketIPInt(getHost()), login, 100, "login server started");
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
			charService.shutdown();
			getFileDescriptorSystem().execute(onDestroyed);
		}
	};

	private final TimerListener onlineDataCleanup = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int tick)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getName()
		{
			return "onlineDataCleanup";
		};

		@Override
		public String toString()
		{
			return getName();
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
			return "onDestroyed";
		}
	};
}

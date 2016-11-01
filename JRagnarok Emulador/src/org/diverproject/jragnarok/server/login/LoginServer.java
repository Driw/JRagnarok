package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOG_LOGIN;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newClientConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newIPBanConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newLogConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newLoginServerConfigs;
import static org.diverproject.jragnarok.JRagnarokUtil.minutes;
import static org.diverproject.log.LogSystem.logInfo;

import org.diverproject.jragnaork.RagnarokException;
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

/**
 * <h1>Servidor de Acesso</h1>
 *
 * <p>Um servidor de acesso é um dos tipos de micro servidores que o JRgarnok disponibiliza.
 * Para esse micro servidor fica sendo de responsabilidade receber a conexão inicial dos jogadores.
 * Quando um jogador tentar se conectar com o servidor, esse micro servidor é quem deverá recebê-los.</p>
 *
 * <p>No servidor de acesso deve ser recebido as informações de acesso da conta (usuário e senha),
 * podendo ser seguido de algumas outras informações como tipo de senha (md5) e versão do cliente.
 * Fica sendo ainda de responsabilidade desse micro servidor informar ao jogador falha de acesso.
 * A falha do acesso poderá ocorrer por usuário ou senha inválida como versão de cliente e afins.</p>
 *
 * <p>Após receber as informações de acesso do jogador, deverá validar o acesso (usuário e senha),
 * tal como possibilidade de uso de client hash ou senhas criptografados em MD5 (hash).
 * Quando validado deverá enviar ao jogador a lista de servidores de personagens disponíveis.</p>
 *
 * @see Server
 * @see ServerListener
 * @see LoginCharServers
 * @see ServiceLoginChar
 * @see ServiceLoginClient
 * @see ServiceLoginIpBan
 * @see ServiceLoginLog
 * @see ServiceLoginServer
 *
 * @author Andrew Mello
 */

public class LoginServer extends Server
{
	/**
	 * Lista dos Servidores de Personagens disponíveis.
	 */
	private LoginCharServers charServers;

	/**
	 * Serviço para comunicação com o servidor de personagens.
	 */
	private ServiceLoginChar charService;

	/**
	 * Serviço para comunicação entre o servidor e o cliente.
	 */
	private ServiceLoginClient clientService;

	/**
	 * Serviço para banimento de acessos por endereço de IP.
	 */
	private ServiceLoginIpBan ipBanService;

	/**
	 * Serviço para registro de acessos.
	 */
	private ServiceLoginLog logService;

	/**
	 * Serviço para para acesso de contas (serviço principal)
	 */
	private ServiceLoginServer loginService;

	/**
	 * Cria um novo micro servidor para receber os novos acessos de clientes.
	 * Define ainda o listener para executar operações durante mudanças de estado.
	 */

	public LoginServer()
	{
		setListener(listener);
	}

	/**
	 * @return aquisição dos servidores de personagens disponíveis para acesso.
	 */

	public LoginCharServers getCharServers()
	{
		return charServers;
	}

	/**
	 * @return aquisição do serviço para comunicação com o servidor de personagens.
	 */

	public ServiceLoginChar getCharService()
	{
		return charService;
	}

	/**
	 * @return aquisição do serviço para comunicação do servidor com o cliente.
	 */

	public ServiceLoginClient getClientService()
	{
		return clientService;
	}

	/**
	 * @return aquisição do serviço para banimento de acessos por endereço de IP.
	 */

	public ServiceLoginIpBan getIpBanService()
	{
		return ipBanService;
	}

	/**
	 * @return aquisição do serviço para registro de acessos no servidor.
	 */

	public ServiceLoginLog getLogService()
	{
		return logService;
	}

	/**
	 * @return aquisição do serviço para acesso de contas (serviço principal)
	 */

	public ServiceLoginServer getLoginService()
	{
		return loginService;
	}

	@Override
	public String getHost()
	{
		return getConfigs().getString(LOGIN_IP);
	}

	@Override
	public int getPort()
	{
		return getConfigs().getInt(LOGIN_PORT);
	}

	/**
	 * Listener que implementa os métodos para alteração de estado do servidor.
	 */

	private final ServerListener listener = new ServerListener()
	{
		@Override
		public void onCreate() throws RagnarokException
		{
			setDefaultConfigs();
		}

		/**
		 * Método que deverá criar as configurações necessárias para funcionamento do servidor.
		 * Por padrão do sistema inicializa as configurações com seus valores padrões.
		 * Após isso deverá vincular as configurações carregadas as configurações do servidor.
		 * @see JRagnarokConfigs
		 */

		private void setDefaultConfigs()
		{
			Configurations log = newLogConfigs();
			Configurations ipban = newIPBanConfigs();
			Configurations client = newClientConfigs();
			Configurations server = newLoginServerConfigs();
	
			Configurations configs = getConfigs();

			if (configs == null)
				setConfigurations(configs = new Configurations());

			configs.add(log);
			configs.add(ipban);
			configs.add(client);
			configs.add(server);
		}

		@Override
		public void onCreated() throws RagnarokException
		{
			charService = new ServiceLoginChar(LoginServer.this);
			clientService = new ServiceLoginClient(LoginServer.this);
			ipBanService = new ServiceLoginIpBan(LoginServer.this);
			logService = new ServiceLoginLog(LoginServer.this);
			loginService = new ServiceLoginServer(LoginServer.this);

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
			logInfo("o servidor de acesso está pronto (porta: %d).\n", getPort());

			Login login = new Login();
			login.setUsername(getConfigs().getString(LOGIN_USERNAME));
			login.setPassword(getConfigs().getString(LOGIN_PASSWORD));

			// TODO confirmar usuário e senha

			logService.addLoginLog(getAddress(), login, 100, "login server started");
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

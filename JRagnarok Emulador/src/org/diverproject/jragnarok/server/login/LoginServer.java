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
	 * Lista dos Servidores de Personagens dispon�veis.
	 */
	private LoginCharServers charServers;

	/**
	 * Servi�o para comunica��o com o servidor de personagens.
	 */
	private ServiceLoginChar charService;

	/**
	 * Servi�o para comunica��o entre o servidor e o cliente.
	 */
	private ServiceLoginClient clientService;

	/**
	 * Servi�o para banimento de acessos por endere�o de IP.
	 */
	private ServiceLoginIpBan ipBanService;

	/**
	 * Servi�o para registro de acessos.
	 */
	private ServiceLoginLog logService;

	/**
	 * Servi�o para para acesso de contas (servi�o principal)
	 */
	private ServiceLoginServer loginService;

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

	public ServiceLoginChar getCharService()
	{
		return charService;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o do servidor com o cliente.
	 */

	public ServiceLoginClient getClientService()
	{
		return clientService;
	}

	/**
	 * @return aquisi��o do servi�o para banimento de acessos por endere�o de IP.
	 */

	public ServiceLoginIpBan getIpBanService()
	{
		return ipBanService;
	}

	/**
	 * @return aquisi��o do servi�o para registro de acessos no servidor.
	 */

	public ServiceLoginLog getLogService()
	{
		return logService;
	}

	/**
	 * @return aquisi��o do servi�o para acesso de contas (servi�o principal)
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
	 * Listener que implementa os m�todos para altera��o de estado do servidor.
	 */

	private final ServerListener listener = new ServerListener()
	{
		@Override
		public void onCreate() throws RagnarokException
		{
			setDefaultConfigs();
		}

		/**
		 * M�todo que dever� criar as configura��es necess�rias para funcionamento do servidor.
		 * Por padr�o do sistema inicializa as configura��es com seus valores padr�es.
		 * Ap�s isso dever� vincular as configura��es carregadas as configura��es do servidor.
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
			logInfo("o servidor de acesso est� pronto (porta: %d).\n", getPort());

			Login login = new Login();
			login.setUsername(getConfigs().getString(LOGIN_USERNAME));
			login.setPassword(getConfigs().getString(LOGIN_PASSWORD));

			// TODO confirmar usu�rio e senha

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

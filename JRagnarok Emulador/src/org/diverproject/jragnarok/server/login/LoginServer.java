package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newClientConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newIPBanConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newLogConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newLoginServerConfigs;
import static org.diverproject.log.LogSystem.logInfo;

import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.login.entities.Account;

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
 * @see LoginServerFacade
 *
 * @author Andrew Mello
 */

public class LoginServer extends Server
{
	/**
	 * Lista dos Servidores de Personagens dispon�veis.
	 */
	private CharServerList charServers;

	/**
	 * Fa�ade contento os servi�os e controles dispon�veis.
	 */
	private LoginServerFacade facade;

	/**
	 * Cria um novo micro servidor para receber os novos acessos de clientes.
	 * Define ainda o listener para executar opera��es durante mudan�as de estado.
	 */

	public LoginServer()
	{
		setListener(LISTENER);
	}

	/**
	 * @return aquisi��o dos servidores de personagens dispon�veis para acesso.
	 */

	public CharServerList getCharServerList()
	{
		return charServers;
	}

	/**
	 * @return aquisi��o do fa�ade que possui os servi�os e controles do servidor de acesso.
	 */

	public LoginServerFacade getFacade()
	{
		return facade;
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

	@Override
	protected LFileDescriptor acceptSocket(Socket socket)
	{
		LFileDescriptor fd = new LFileDescriptor(socket);
		fd.setParseListener(facade.PARSE_CLIENT);

		return fd;
	}

	/**
	 * Listener que implementa os m�todos para altera��o de estado do servidor.
	 */

	private final ServerListener LISTENER = new ServerListener()
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
			charServers = new CharServerList();

			facade = new LoginServerFacade();
			facade.create(LoginServer.this);
		}

		@Override
		public void onRunning() throws RagnarokException
		{
			logInfo("o servidor de acesso est� pronto (porta: %d).\n", getPort());

			String username = getConfigs().getString(LOGIN_USERNAME);
			String password = getConfigs().getString(LOGIN_PASSWORD);

			Account account = getFacade().getAccountControl().get(username);

			if (account == null)
				throw new RagnarokException("%s usando '%s' e n�o foi encontrado", LOGIN_USERNAME, username);

			if (!account.getPassword().equals(password))
				throw new RagnarokException("%s uasndo '%s' e n�o combinou", LOGIN_PASSWORD, password);

			facade.getLogService().add(getAddress(), account, 100, "login server started");
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
			for (ClientCharServer client : charServers)
				client.getFileDecriptor().close();

			charServers.clear();
			facade.destroy(LoginServer.this);
		}

		@Override
		public void onDestroyed() throws RagnarokException
		{
			facade.destroyed();

			charServers = null;
			facade = null;
		}
	};
}

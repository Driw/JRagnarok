package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_CHAR_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newCharServerConfigs;
import static org.diverproject.log.LogSystem.logInfo;

import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;
import org.diverproject.jragnarok.server.login.CharServerList;

/**
 * <h1>Servidor de Personagem</h1>
 *
 * <p>Este servidor ser� respons�vel por realizar o controle de todas as informa��es dos personagens.
 * Essas informa��es v�o deste de identifica��es, nomes, n�veis como rela��es entre os personagens:
 * amizades, grupos, cl� e v�nculos como mascotes, hom�nculos, elementares e assistentes.</p>
 *
 * <p>O servidor de personagem tamb�m dever� possuir os servi�os adequados para realizar seus deveres.
 * Para isso h� um servi�o de comunica��o inicial com o cliente afim de receber dados e comandos.
 * Sempre que um novo servidor de personagem criado ele dever� ser autenticado no servidor de acesso.</p>
 *
 * <p>Para que ele esteja realmente funcionando dever� estar constantemente conectado com um
 * servidor de acesso especificado nas configura��es, pois algumas informa��es ser�o obtidas dele.
 * Por exemplo, alguns dados da conta do jogador necess�rios ser�o solicitados do mesmo.</p>
 *
 * @see Server
 * @see MapServerList
 * @see CharServerList
 *
 * @author Andrew
 */

public class CharServer extends Server
{
	/**
	 * Lista dos Servidores de Mapa dispon�veis.
	 */
	private MapServerList mapServers;

	/**
	 * Fa�ade contento os servi�os e controles dispon�veis.
	 */
	private CharServerFacade facade;

	/**
	 * Cria uma nova inst�ncia de um servidor de personagem inicializa��o o seu listener.
	 */

	public CharServer()
	{
		setListener(listener);
	}

	/**
	 * @return aquisi��o da lista dos servidores de mapa dispon�veis.
	 */

	public MapServerList getMapServers()
	{
		return mapServers;
	}

	/**
	 * @return aquisi��o do fa�ade que possui os servi�os e controles do servidor de personagem.
	 */

	public CharServerFacade getFacade()
	{
		return facade;
	}

	@Override
	public String getHost()
	{
		return getConfigs().getString(CHAR_IP);
	}

	@Override
	public int getPort()
	{
		return getConfigs().getInt(CHAR_PORT);
	}

	@Override
	public String getDefaultConfigs()
	{
		return getConfigs().getString(SYSTEM_SERVER_DEFAULT_CHAR_FILES);
	}

	@Override
	protected CFileDescriptor acceptSocket(Socket socket)
	{
		CFileDescriptor fd = new CFileDescriptor(socket);
		fd.setParseListener(facade.CLIENT_PARSE);
		fd.setCloseListener(facade.CLOSE_LISTENER);

		return fd;
	}

	/**
	 * Listener que ir� executar as opera��es necess�rias conforme mudan�as de estado do servidor.
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
			Configurations server = newCharServerConfigs();
	
			Configurations configs = getConfigs();

			if (configs == null)
				setConfigurations(configs = new Configurations());

			configs.add(server);
		}

		@Override
		public void onCreated() throws RagnarokException
		{
			mapServers = new MapServerList();
			facade = new CharServerFacade();
		};

		@Override
		public void onRunning() throws RagnarokException
		{
			facade.init(CharServer.this);

			logInfo("o servidor de personagem est� pronto (porta: %d).\n", getPort());
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
			facade.destroy();
		}

		@Override
		public void onDestroyed() throws RagnarokException
		{
			facade.destroyed();

			facade = null;
			mapServers = null;
		}
	};
}

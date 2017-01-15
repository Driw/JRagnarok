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
 * <p>Este servidor será responsável por realizar o controle de todas as informações dos personagens.
 * Essas informações vão deste de identificações, nomes, níveis como relações entre os personagens:
 * amizades, grupos, clã e vínculos como mascotes, homúnculos, elementares e assistentes.</p>
 *
 * <p>O servidor de personagem também deverá possuir os serviços adequados para realizar seus deveres.
 * Para isso há um serviço de comunicação inicial com o cliente afim de receber dados e comandos.
 * Sempre que um novo servidor de personagem criado ele deverá ser autenticado no servidor de acesso.</p>
 *
 * <p>Para que ele esteja realmente funcionando deverá estar constantemente conectado com um
 * servidor de acesso especificado nas configurações, pois algumas informações serão obtidas dele.
 * Por exemplo, alguns dados da conta do jogador necessários serão solicitados do mesmo.</p>
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
	 * Lista dos Servidores de Mapa disponíveis.
	 */
	private MapServerList mapServers;

	/**
	 * Façade contento os serviços e controles disponíveis.
	 */
	private CharServerFacade facade;

	/**
	 * Cria uma nova instância de um servidor de personagem inicialização o seu listener.
	 */

	public CharServer()
	{
		setListener(listener);
	}

	/**
	 * @return aquisição da lista dos servidores de mapa disponíveis.
	 */

	public MapServerList getMapServers()
	{
		return mapServers;
	}

	/**
	 * @return aquisição do façade que possui os serviços e controles do servidor de personagem.
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
	 * Listener que irá executar as operações necessárias conforme mudanças de estado do servidor.
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

			logInfo("o servidor de personagem está pronto (porta: %d).\n", getPort());
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

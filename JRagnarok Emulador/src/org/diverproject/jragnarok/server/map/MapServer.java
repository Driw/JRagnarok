package org.diverproject.jragnarok.server.map;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_MAP_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newMapServerConfigs;
import static org.diverproject.log.LogSystem.logInfo;

import java.net.Socket;

import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.jragnarok.configs.MapServerConfigs;
import org.diverproject.jragnarok.configuration.Configurations;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;

public class MapServer extends Server
{
	/**
	 * Acesso r�pido as configura��es do servidor de mapa.
	 */
	private MapServerConfigs mapServerConfigs;

	/**
	 * Fa�ade contento os servi�os e controles dispon�veis.
	 */
	private MapServerFacade facade;

	/**
	 * Cria um novo micro servidor para receber os personagens selecionados pelos jogadores.
	 * Define ainda o listener para executar opera��es durante mudan�as de estado.
	 */

	public MapServer()
	{
		setListener(listener);
	}

	/**
	 * @return aquisi��o do acesso r�pido as configura��es do servidor de mapa.
	 */

	public MapServerConfigs getMapServerConfigs()
	{
		return mapServerConfigs;
	}

	/**
	 * @return aquisi��o do fa�ade que possui os servi�os e controles do servidor de mapa.
	 */

	public MapServerFacade getFacade()
	{
		return facade;
	}

	@Override
	public String getHost()
	{
		return getConfigs().getString(MAP_IP);
	}

	@Override
	public int getPort()
	{
		return getConfigs().getInt(MAP_PORT);
	}

	@Override
	public String getDefaultConfigs()
	{
		return getConfigs().getString(SYSTEM_SERVER_DEFAULT_MAP_FILES);
	}

	@Override
	protected FileDescriptor acceptSocket(Socket socket)
	{
		MFileDescriptor fd = new MFileDescriptor(socket);
		fd.setParseListener(facade.PARSE_CLIENT);
		fd.setCloseListener(facade.CLOSE_LISTENER);

		return fd;
	}

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
			Configurations server = newMapServerConfigs();
			Configurations configs = getConfigs();

			if (configs == null)
				setConfigurations(configs = new Configurations());

			configs.add(server);
		}

		@Override
		public void onCreated() throws RagnarokException
		{
			facade = new MapServerFacade();
		}

		@Override
		public void onRunning() throws RagnarokException
		{
			MapServer.this.mapServerConfigs = new MapServerConfigs(getConfigs());
			facade.init(MapServer.this);

			logInfo("o servidor de mapa est� pronto (porta: %d).\n", getPort());
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
			facade.destroy(MapServer.this);
		}

		@Override
		public void onDestroyed() throws RagnarokException
		{
			facade.destroyed();
			facade = null;

			MapServer.this.mapServerConfigs = null;
		}
	};

	@Override
	protected void update(int now, int tick)
	{
		facade.getServiceMapChar().update(now, tick);
	}
}

package org.diverproject.jragnarok.server.map;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newMapServerConfigs;

import java.net.Socket;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;

public class MapServer extends Server
{
	public MapServer()
	{
		setListener(listener);
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
	protected FileDescriptor acceptSocket(Socket socket)
	{
		MFileDescriptor fd = new MFileDescriptor(socket);
		fd.setParseListener(null/* TODO */);

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
		 * Método que deverá criar as configurações necessárias para funcionamento do servidor.
		 * Por padrão do sistema inicializa as configurações com seus valores padrões.
		 * Após isso deverá vincular as configurações carregadas as configurações do servidor.
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
			
		};

		@Override
		public void onRunning() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
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
	};
}

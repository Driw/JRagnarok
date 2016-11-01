package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newCharServerConfigs;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;

public class CharServer extends Server
{
	public CharServer()
	{
		setListener(listener);
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

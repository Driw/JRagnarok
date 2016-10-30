package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.Server;
import org.diverproject.jragnarok.server.ServerListener;

public class CharServer extends Server
{
	public CharServer()
	{
		setListener(listener);
	}

	private final ServerListener listener = new ServerListener()
	{
		@Override
		public void onStoped() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onStop() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onRunning() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onDestroyed() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onDestroy() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCreated() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCreate() throws RagnarokException
		{
			// TODO Auto-generated method stub
			
		}
	};
}

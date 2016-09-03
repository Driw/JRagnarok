package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.jragnarok.server.RagnarokPacketNames.PACKET_SYNCRONIZE_IPADDRESS;
import static org.diverproject.log.LogSystem.logInfo;

import org.diverproject.jragnarok.server.ClientCharServer;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.util.stream.Buffer;
import org.diverproject.util.stream.implementation.buffer.BufferArrayData;
import org.diverproject.util.stream.implementation.output.OutputPacket;

public class LoginCharacterService extends LoginServerService
{
	public LoginCharacterService(LoginServer server)
	{
		super(server);
	}

	private LoginCharacterService getSelf()
	{
		return this;
	}

	private TimerListener syncronizeIpAddress = new TimerListener()
	{
		private LoginCharacterService self = getSelf();

		@Override
		public void onCall(Timer timer, long tick)
		{
			logInfo("Sincronização de IP em progresso...\n");

			Buffer buffer = new BufferArrayData(new byte[2]);
			buffer.putShort(PACKET_SYNCRONIZE_IPADDRESS);
			self.sendAllWOS(-1, buffer);
		}
	};

	private int sendAllWOS(int clientIgnore, Buffer buffer)
	{
		int count = 0;

		for (ClientCharServer charServer : getServer().getCharServers())
		{
			if (charServer.getID() != clientIgnore)
			{
				OutputPacket output = charServer.newOutputPacket("SyncIpAddress", buffer.length());
				output.putBytes(buffer.getArrayBuffer());
				output.flush();

				count++;
			}
		}

		return count;
	}

	public void init()
	{
		int interval = getConfigs().getInt("login.ip_sync_interval");

		if (interval > 0)
		{
			TimerSystem ts = TimerSystem.getInstance();
			ts.addListener(syncronizeIpAddress, "sync_ip_addresses");
			ts.addTimerInterval(ts.tick() + interval, syncronizeIpAddress, 0, 0, interval);
		}
	}

	public void shutdown()
	{
		// TODO Auto-generated method stub
		
	}
}

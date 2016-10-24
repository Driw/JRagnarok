package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.log.LogSystem.logInfo;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.SyncronizeAddressPacket;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.util.ObjectDescription;

public class LoginCharacterService extends LoginServerService
{
	public LoginCharacterService(LoginServer server)
	{
		super(server);
	}

	private TimerListener syncronizeIpAddress = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int tick)
		{
			logInfo("Sincronização de IP em progresso...\n");

			SyncronizeAddressPacket packet = new SyncronizeAddressPacket();
			sendAllWithoutOurSelf(-1, packet);
		}

		@Override
		public String getName()
		{
			return "syncronizeIpAddress";
		}

		@Override
		public String toString()
		{
			ObjectDescription description = new ObjectDescription(getClass());

			description.append(getName());

			return description.toString();
		}
	};

	private int sendAllWithoutOurSelf(int ignoreFileDecriptID, ResponsePacket packet)
	{
		int count = 0;

		for (ClientCharServer server : getServer().getCharServers())
		{
			if (server.getFileDecriptor().getID() != ignoreFileDecriptID)
			{
				packet.send(server.getFileDecriptor());
				count++;
			}
		}

		return count;
	}

	public FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			// TODO Auto-generated method stub

			return true;
		}
	};

//	keepAlive
//	onDisconnect

//	serverDestroy
//	serverInit
//	serverReset

//	acknologeUserCount
//	setAccountOffline
//	setAccountOnline
//	setAllOffline

//	updateCharIP
//	updateOnlineDatabase
//	updatePincode

//	authenticate

//	accountDataResquest
//	accountDataSend
//	accountInfo
//	vipDataResquest
//	vipDataSend
//	banAccountRequest
//	unbanAccountRequest
//	updateAccountSate
//	requestChangeEmail
//	requestChangeSex
//	pincodeAuthFail
//	globalAccountRegResquest
//	globalAccountRegUpdate

	public void init()
	{
		int interval = getConfigs().getInt("login.ip_sync_interval");

		if (interval > 0)
		{
			TimerSystem ts = TimerSystem.getInstance();
			TimerMap timers = ts.getTimers();

			Timer siaTimer = ts.acquireTimer();
			siaTimer.setListener(syncronizeIpAddress);
			siaTimer.setTick(ts.tick() + interval);
			siaTimer.setInterval(interval);
			timers.add(siaTimer);
		}
	}

	public void shutdown()
	{
		// TODO Auto-generated method stub
		
	}
}

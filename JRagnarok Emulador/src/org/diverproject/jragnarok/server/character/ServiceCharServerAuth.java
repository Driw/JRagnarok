package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.receive.CharServerSelected;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.character.entities.AuthNode;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.jragnarok.server.character.structures.OnlineCharData;
import org.diverproject.jragnarok.server.common.NotifyAuthResult;
import org.diverproject.util.stream.Output;
import org.diverproject.util.stream.StreamException;

public class ServiceCharServerAuth extends AbstractCharService
{
	public ServiceCharServerAuth(CharServer server)
	{
		super(server);
	}

	/**
	 * Procedimento de chamada em um temporizador para remover um jogador online do sistema.
	 * Isso não irá notificar o jogador de que ficou online, apenas remover do sistema.
	 */

	private final TimerListener waitingDisconnect = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			OnlineCharData online = onlines.get(timer.getObjectID());
			onlines.remove(online);
		}
		
		@Override
		public String getName()
		{
			return "waitingDisconnect";
		}
	};

	public boolean parse(CFileDescriptor fd)
	{
		CharServerSelected packet = new CharServerSelected();
		packet.receive(fd);

		if (fd.getSessionData().getID() == 0)
		{
			logNotice("conexão solicitada (aid: %d, seed: %d|%d).\n", packet.getAccountID(), packet.getFirstSeed(), packet.getSecondSeed());
			return true;
		}

		try {

			Output output = fd.getPacketBuilder().newOutputPacket("CS_SELECTED_BACK", 4);
			output.putInt(packet.getAccountID());
			output.flush();
			output = null;

		} catch (StreamException e) {
			throw new RagnarokRuntimeException(e.getMessage());
		}

		return parseRequest(fd);
	}

	private boolean parseRequest(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		AuthNode node = auths.get(sd.getID());

		if (node != null && node.getAccountID() == sd.getID() && node.getSeed().equals(sd.getSeed()))
		{
			sd.setVersion(node.getVersion());
			auths.remove(node.getAccountID());

			authOk(fd);
		}

		else
			login.reqAuthAccount(fd);

		return true;
	}

	private void authOk(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		OnlineCharData online = onlines.get(sd.getID());

		if (online != null)
		{
			// Personagem online, dar kick do servidor
			if (online.getServer() > -1)
			{
				// TODO char.c:1891

				if (online.getWaitingDisconnect() == null)
				{
					TimerMap timers = getTimerSystem().getTimers();

					Timer timer = timers.acquireTimer();
					timer.setListener(waitingDisconnect);
					timer.setObjectID(online.getAccountID());
					timers.addInterval(timer, seconds(20));

					online.setWaitingDisconnect(timer);
				}

				client.sendNotifyResult(fd, NotifyAuthResult.RECOGNIZES_LAST_LOGIN);
				return;
			}

			// Já está conectado mas não selecionou um personagem
			if (online.getFileDescriptor() != null && online.getFileDescriptor().getID() != fd.getID())
			{
				client.sendNotifyResult(fd, NotifyAuthResult.RECOGNIZES_LAST_LOGIN);
				return;
			}

			online.setFileDescriptor(fd);
		}

		sd.setAuth(true);
		login.reqAccountData(fd);
		character.setCharSelect(fd);
	}
}

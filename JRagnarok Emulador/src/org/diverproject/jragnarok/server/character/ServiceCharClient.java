package org.diverproject.jragnarok.server.character;

import static org.diverproject.log.LogSystem.logDebug;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.response.RefuseEnter;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.character.control.OnlineCharControl;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.jragnarok.server.character.structures.OnlineCharData;

public class ServiceCharClient extends AbstractCharService
{
	private OnlineCharControl onlines;

	public ServiceCharClient(CharServer server)
	{
		super(server);
	}

	public void init() throws RagnarokException
	{
		onlines = new OnlineCharControl(getConnection());
	}

	public void destroy()
	{
		onlines.clear();
		onlines = null;
	}

	/**
	 * Listener usado para receber novas conexões solicitadas com o servidor de personagem.
	 */

	public final FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			logDebug("parsing fd#%d.\n", fd.getID());

			if (!fd.isConnected())
				return false;

			// Já conectou, verificar se está banido
			if (fd.getCache() != null)
				return parseAlreadyAuth(fd);

			return acknowledgePacket(fd);
		}
	};

	private boolean acknowledgePacket(FileDescriptor fd)
	{
		return false;
	}

	private boolean parseAlreadyAuth(FileDescriptor fd)
	{
		Object cache = fd.getCache();
		CharSessionData sd = (CharSessionData) cache;
		OnlineCharData online = onlines.get(sd.getAccountID());

		if (online != null && online.getFileDescriptor().getID() == fd.getID())
			online.setFileDescriptor(null);

		// Se não está em nenhum servidor deixar offline.
		if (online == null || online.getServer() == -1)
			onlines.remove(online);

		return false;
	}

	public void refuseEnter(FileDescriptor fd, byte result)
	{
		RefuseEnter packet = new RefuseEnter();
		packet.setResult(result);
		packet.send(fd);
	}

	public boolean charAuthOk(FileDescriptor fd, CharSessionData sd)
	{
		// TODO Auto-generated method stub
		return false;
	}
}

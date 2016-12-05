package org.diverproject.jragnarok.server.character;

import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.receive.CharServerSelected;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.character.control.AuthControl;
import org.diverproject.jragnarok.server.character.entities.AuthNode;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.util.stream.Output;
import org.diverproject.util.stream.StreamException;

public class ServiceCharServerAuth extends AbstractCharService
{
	private AuthControl control;

	public ServiceCharServerAuth(CharServer server)
	{
		super(server);
	}

	public void init()
	{
		control = getServer().getAuthControl();
	}

	public void destroy()
	{
		control = null;
	}

	public boolean parse(CFileDescriptor fd)
	{
		CharServerSelected packet = new CharServerSelected();
		packet.receive(fd);

		CharSessionData sd = fd.getSessionData();

		if (sd != null)
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

		return parseRequest(fd, sd);
	}

	private boolean parseRequest(FileDescriptor fd, CharSessionData sd)
	{
		AuthNode node = control.get(sd.getID());

		if (node != null && node.getAccountID() == sd.getID() && node.getSeed().equals(sd.getSeed()))
		{
			sd.setVersion(node.getVersion());
			control.remove(node.getAccountID());

			authOk(fd, sd);
		}

		//else
			//client.reqAuthAccount(fd, sd);

		return true;
	}

	private void authOk(FileDescriptor fd, CharSessionData sd)
	{
		
	}
}

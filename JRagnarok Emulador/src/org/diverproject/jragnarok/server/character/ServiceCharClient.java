package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.packets.response.RefuseEnter;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;

public class ServiceCharClient extends AbstractCharService
{
	public ServiceCharClient(CharServer server)
	{
		super(server);
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
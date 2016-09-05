package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDecriptor;
import org.diverproject.util.stream.implementation.input.InputPacket;

public abstract class ReceivePacket extends GenericPacket
{
	public final void receive(FileDecriptor fd)
	{
		InputPacket input = fd.newInput(getName());
		receiveInput(input);
	}

	protected abstract void receiveInput(InputPacket input);
}

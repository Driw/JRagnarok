package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.stream.Input;

public abstract class ReceivePacket extends AbstractPacket implements IReceivePacket
{
	private AbstractReceivePacket receive;

	public ReceivePacket()
	{
		receive = new AbstractReceivePacket(this)
		{
			@Override
			protected void receiveInput(Input input)
			{
				ReceivePacket.this.receiveInput(input);
			}
		};
	}

	@Override
	public void receive(FileDescriptor fd)
	{
		receive.receive(fd);
	}

	@Override
	public void receive(FileDescriptor fd, boolean validate)
	{
		receive.receive(fd, validate);
	}

	protected abstract void receiveInput(Input input);
}

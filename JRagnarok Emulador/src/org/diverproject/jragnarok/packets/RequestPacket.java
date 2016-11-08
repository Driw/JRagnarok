package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public abstract class RequestPacket extends AbstractPacket implements IReceivePacket, IResponsePacket
{
	private AbstractReceivePacket receive;
	private AbstractResponsePacket response;

	public RequestPacket()
	{
		receive = new AbstractReceivePacket(this)
		{
			@Override
			protected void receiveInput(Input input)
			{
				RequestPacket.this.receiveInput(input);
			}
		};

		response = new AbstractResponsePacket(this)
		{
			@Override
			protected void sendOutput(Output output)
			{
				RequestPacket.this.sendOutput(output);
			}
		};
	}

	@Override
	public void send(FileDescriptor fd)
	{
		response.send(fd);
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

	protected abstract void sendOutput(Output output);
	protected abstract void receiveInput(Input input);
}

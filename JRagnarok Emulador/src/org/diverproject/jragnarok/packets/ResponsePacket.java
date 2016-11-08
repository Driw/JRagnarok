package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.stream.Output;

public abstract class ResponsePacket extends AbstractPacket implements IResponsePacket
{
	private AbstractResponsePacket response;

	public ResponsePacket()
	{
		response = new AbstractResponsePacket(this)
		{
			@Override
			protected void sendOutput(Output output)
			{
				ResponsePacket.this.sendOutput(output);
			}
		};
	}

	@Override
	public void send(FileDescriptor fd)
	{
		response.send(fd);
	}

	protected abstract void sendOutput(Output output);
}

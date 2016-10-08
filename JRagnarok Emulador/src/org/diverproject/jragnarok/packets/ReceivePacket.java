package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.StreamRuntimeException;
import org.diverproject.util.stream.implementation.PacketBuilder;

public abstract class ReceivePacket extends GenericPacket
{
	public final void receive(FileDescriptor fd)
	{
		Input input;
		PacketBuilder builder = fd.getPacketBuilder();

		try {

			if (length() == 0)
				input = builder.newInputPacket(getName());
			else
				input = builder.newInputPacket(getName(), length());

			receiveInput(input);

		} catch (StreamException e) {
			throw new StreamRuntimeException("falha ao receber %s (ip: %s)", fd.getAddressString());
		}
	}

	protected abstract void receiveInput(Input input);
	protected abstract int length();
}

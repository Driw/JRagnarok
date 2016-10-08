package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.stream.Output;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.StreamRuntimeException;
import org.diverproject.util.stream.implementation.PacketBuilder;

public abstract class ResponsePacket extends GenericPacket
{
	public final void send(FileDescriptor fd)
	{
		Output output;
		PacketBuilder builder = fd.getPacketBuilder();

		try {

			if (length() == 0)
				output = builder.newOutputPacket(getName());
			else
				output = builder.newOutputPacket(getName(), length());

			sendOutput(output);

		} catch (StreamException e) {
			throw new StreamRuntimeException("falha ao enviar %s (ip: %s)", nameOf(this), fd.getAddressString());
		}
	}

	protected abstract void sendOutput(Output output);
	protected abstract int length();
}

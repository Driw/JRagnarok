package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.stream.Output;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.StreamRuntimeException;
import org.diverproject.util.stream.implementation.PacketBuilder;

public abstract class AbstractResponsePacket implements IResponsePacket
{
	private AbstractPacket packet;

	public AbstractResponsePacket(AbstractPacket packet)
	{
		this.packet = packet;
	}

	public final void send(FileDescriptor fd)
	{
		Output output;
		PacketBuilder builder = fd.getPacketBuilder();

		try {

			if (packet.length() == 0)
				output = builder.newOutputPacket(packet.getName());
			else
				output = builder.newOutputPacket(packet.getName(), packet.length() + 2);

			output.setInvert(true);
			output.putShort(packet.getIdentify());
			sendOutput(output);

			output.flush();

		} catch (StreamException e) {
			throw new StreamRuntimeException("falha ao enviar %s (ip: %s)", nameOf(this), fd.getAddressString());
		}
	}

	protected abstract void sendOutput(Output output);
}

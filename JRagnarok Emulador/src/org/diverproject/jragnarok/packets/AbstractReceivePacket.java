package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.lang.HexUtil;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.StreamException;
import org.diverproject.util.stream.StreamRuntimeException;
import org.diverproject.util.stream.implementation.PacketBuilder;

public abstract class AbstractReceivePacket implements IReceivePacket
{
	private AbstractPacket packet;

	public AbstractReceivePacket(AbstractPacket packet)
	{
		this.packet = packet;
	}

	public final void receive(FileDescriptor fd)
	{
		receive(fd, false);
	}

	public final void receive(FileDescriptor fd, boolean validate)
	{
		Input input;
		PacketBuilder builder = fd.getPacketBuilder();

		try {

			if (packet.length() == 0)
				input = builder.newInputPacket(packet.getName());
			else
				input = builder.newInputPacket(packet.getName(), packet.length() + (validate ? 2 : 0));

			if (validate)
				validate(input);

			receiveInput(input);

		} catch (StreamException e) {
			throw new StreamRuntimeException("falha ao receber %s (ip: %s)", fd.getAddressString());
		}
	}

	void validate(Input input)
	{
		short id = input.getShort();

		if (id != packet.getIdentify())
			throw new UnknowPacketException("esperado %s e recebido %s", packet.getHexIdentify(), HexUtil.parseInt(id, 4));
	}

	protected abstract void receiveInput(Input input);
}

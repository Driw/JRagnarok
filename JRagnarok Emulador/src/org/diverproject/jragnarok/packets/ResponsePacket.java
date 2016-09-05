package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDecriptor;
import org.diverproject.util.stream.implementation.output.OutputPacket;

public abstract class ResponsePacket extends GenericPacket
{
	public final void send(FileDecriptor fd)
	{
		OutputPacket output = fd.newOutput(getName());
		output.putShort(getIdentify());

		sendOutput(output);
	}

	protected abstract void sendOutput(OutputPacket output);
}

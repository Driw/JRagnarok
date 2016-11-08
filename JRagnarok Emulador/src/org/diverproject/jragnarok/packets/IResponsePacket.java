package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDescriptor;

public interface IResponsePacket
{
	void send(FileDescriptor fd);
}

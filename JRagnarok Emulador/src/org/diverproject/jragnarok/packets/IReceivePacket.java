package org.diverproject.jragnarok.packets;

import org.diverproject.jragnarok.server.FileDescriptor;

public interface IReceivePacket
{
	void receive(FileDescriptor fd);
	void receive(FileDescriptor fd, boolean validate);
}

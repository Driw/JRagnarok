package org.diverproject.jragnarok.server;

import org.diverproject.jragnaork.RagnarokException;

public interface SocketParse
{
	void parse(FileDecriptor decriptor) throws RagnarokException;
}

package org.diverproject.jragnarok.server;

import org.diverproject.jragnaork.RagnarokException;

public interface FileDecriptorListener
{
	void onCall(FileDecriptor decriptor) throws RagnarokException;
}

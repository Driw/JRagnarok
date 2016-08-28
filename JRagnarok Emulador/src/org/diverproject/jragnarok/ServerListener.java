package org.diverproject.jragnarok;

import org.diverproject.jragnaork.RagnarokException;

public interface ServerListener
{
	void onCreate() throws RagnarokException;
	void onCreated() throws RagnarokException;
	void onRunning() throws RagnarokException;
	void onStop() throws RagnarokException;
	void onStoped() throws RagnarokException;
	void onDestroy() throws RagnarokException;
	void onDestroyed() throws RagnarokException;
}

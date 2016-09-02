package org.diverproject.jragnarok.server;

public class ClientHash
{
	private int groupID;
	private final byte hash[];

	public ClientHash()
	{
		hash = new byte[16];
	}

	public int getGroupID()
	{
		return groupID;
	}

	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}

	public byte[] getHash()
	{
		return hash;
	}
}

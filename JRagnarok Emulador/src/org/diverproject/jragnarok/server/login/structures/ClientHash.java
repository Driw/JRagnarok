package org.diverproject.jragnarok.server.login.structures;

public class ClientHash
{
	private final byte hash[];

	public ClientHash()
	{
		hash = new byte[16];
	}

	public byte[] getHash()
	{
		return hash;
	}
}

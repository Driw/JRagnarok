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

	public void set(byte[] hashValue)
	{
		if (hashValue.length == 16)
			for (int i = 0; i < hashValue.length; i++)
				hash[i] = hashValue[i];
	}
}

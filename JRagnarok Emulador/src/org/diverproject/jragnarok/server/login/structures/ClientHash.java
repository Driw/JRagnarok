package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.util.ObjectDescription;

public class ClientHash
{
	public static final int SIZE = 16;

	private final byte hash[];
	private String hashString;

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
		if (hashValue.length == SIZE)
		{
			for (int i = 0; i < hashValue.length; i++)
				hash[i] = hashValue[i];

			hashString = new String(hash);
		}
	}

	public String getHashString()
	{
		return hashString;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof ClientHash)
		{
			ClientHash clientHash = (ClientHash) obj;

			for (int i = 0; i < SIZE; i++)
			{
				if (hash[i] != clientHash.hash[i])
					return false;

				if (hash[i] == 0)
					break;
			}

			return true;
		}

		return false;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append(getHashString());

		return description.toString();
	}
}

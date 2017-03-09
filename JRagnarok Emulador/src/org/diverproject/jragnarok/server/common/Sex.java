package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.RagnarokRuntimeException;

public enum Sex
{
	FEMALE('F'),
	MALE('M'),
	SERVER('S');

	public final char c;

	private Sex(char c)
	{
		this.c = c;
	}

	public byte code()
	{
		return b(ordinal());
	}

	public static final Sex parse(char c)
	{
		switch (c)
		{
			case 'F': return FEMALE;
			case 'M': return MALE;
			case 'S': return SERVER;
		}

		throw new RagnarokRuntimeException("Sex#%s não encontrado", c);
	}

	public static final Sex parse(int code)
	{
		switch (code)
		{
			case 0: return FEMALE;
			case 1: return MALE;
			case 2: return SERVER;
		}

		throw new RagnarokRuntimeException("Sex#%d não encontrado", code);
	}
}

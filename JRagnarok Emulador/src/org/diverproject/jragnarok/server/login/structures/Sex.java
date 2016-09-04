package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.jragnaork.RagnarokException;

public enum Sex
{
	SERVER,
	FEMALE,
	MALE;

	public static Sex parse(char c) throws RagnarokException
	{
		c = Character.toUpperCase(c);

		switch (c)
		{
			case 'S': return SERVER;
			case 'F': return FEMALE;
			case 'M': return MALE;
		}

		throw new RagnarokException("%s não é um LoginSex", Character.toString(c));
	}

	public static char cast(Sex sex) throws RagnarokException
	{
		switch (sex)
		{
			case SERVER: return 'S';
			case FEMALE: return 'F';
			case MALE: return 'M';
		}

		throw new RagnarokException("sex nulo");
	}
}

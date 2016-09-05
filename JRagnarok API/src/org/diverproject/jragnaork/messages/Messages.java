package org.diverproject.jragnaork.messages;

import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

public class Messages
{
	public static final Messages INSTANCE = new Messages();

	private Map<Integer, String> loginMessages;
	private Map<Integer, String> charMessages;
	private Map<Integer, String> mapMessages;

	public Messages()
	{
		loginMessages = new IntegerLittleMap<>();
		charMessages = new IntegerLittleMap<>();
		mapMessages = new IntegerLittleMap<>();
	}

	public Map<Integer, String> getLoginMessages()
	{
		return loginMessages;
	}

	public Map<Integer, String> getCharMessages()
	{
		return charMessages;
	}

	public Map<Integer, String> getMapMessages()
	{
		return mapMessages;
	}

	public static Messages getInstance()
	{
		return INSTANCE;
	}
}

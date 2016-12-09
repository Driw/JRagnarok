package org.diverproject.jragnarok.server.character.structures;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

public class CharData
{
	private int id;
	private int charMove;
	private Time unban;

	public CharData()
	{
		unban = new Time();
	}

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public int getCharMove()
	{
		return charMove;
	}

	public void setCharMove(int charMove)
	{
		this.charMove = charMove;
	}

	public Time getUnban()
	{
		return unban;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("charMove", charMove);
		description.append("uban", unban);

		return description.toString();
	}
}

package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_CHAR_MAP_USER_COUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class CharMapUserCountRequest extends RequestPacket
{
	private int users;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(users);
	}

	@Override
	protected void receiveInput(Input input)
	{
		users = input.getInt();
	}

	public int getUsers()
	{
		return users;
	}

	public void setUsers(int users)
	{
		this.users = users;
	}

	@Override
	public String getName()
	{
		return "REQ_CHAR_MAP_USER_COUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REQ_CHAR_MAP_USER_COUNT;
	}

	@Override
	protected int length()
	{
		return 4;
	}
}

package org.diverproject.jragnarok.packets.inter.charmap;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HZ_SET_USERS_COUNT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HZ_SetUsersCount extends RequestPacket
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
		return "HZ_SET_USERS_COUNT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HZ_SET_USERS_COUNT;
	}

	@Override
	protected int length()
	{
		return 4;
	}
}

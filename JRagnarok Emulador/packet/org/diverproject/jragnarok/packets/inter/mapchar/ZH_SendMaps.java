package org.diverproject.jragnarok.packets.inter.mapchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ZH_SEND_MAPS;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class ZH_SendMaps extends RequestPacket
{
	private Queue<Integer> maps;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(maps.size());

		while (!maps.isEmpty())
			output.putInt(maps.poll());
	}

	@Override
	protected void receiveInput(Input input)
	{
		maps = new DynamicQueue<>();
		int size = input.getInt();

		for (int i = 0; i < size; i++)
			maps.offer(input.getInt());
	}

	public Queue<Integer> getMaps()
	{
		return maps;
	}

	public void setMaps(Queue<Integer> maps)
	{
		this.maps = maps;
	}

	@Override
	public String getName()
	{
		return "ZH_SEND_MAPS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ZH_SEND_MAPS;
	}

	@Override
	protected int length()
	{
		return DYNAMIC_PACKET_LENGTH;
	}
}

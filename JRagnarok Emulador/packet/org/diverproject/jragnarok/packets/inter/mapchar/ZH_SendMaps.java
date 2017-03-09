package org.diverproject.jragnarok.packets.inter.mapchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ZH_SEND_MAPS;

import org.diverproject.jragnarok.database.impl.MapIndex;
import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class ZH_SendMaps extends RequestPacket
{
	private Queue<MapIndex> maps;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(maps.size());

		while (!maps.isEmpty())
		{
			MapIndex map = maps.poll();

			output.putInt(map.getID());
			output.putString(map.getMapName());
		}
	}

	@Override
	protected void receiveInput(Input input)
	{
		maps = new DynamicQueue<>();
		int size = input.getInt();

		for (int i = 0; i < size; i++)
		{
			MapIndex map = new MapIndex();
			map.setID(input.getInt());
			map.setMapName(input.getString());
			maps.offer(map);
		}
	}

	public Queue<MapIndex> getMaps()
	{
		return maps;
	}

	public void setMaps(Queue<MapIndex> maps)
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

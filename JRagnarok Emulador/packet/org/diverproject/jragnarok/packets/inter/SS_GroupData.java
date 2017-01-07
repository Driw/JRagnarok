package org.diverproject.jragnarok.packets.inter;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SS_GROUP_DATA;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.jragnarok.server.common.entities.GroupCommands;
import org.diverproject.jragnarok.server.common.entities.GroupPermissions;
import org.diverproject.jragnarok.server.common.entities.Vip;
import org.diverproject.util.collection.Map.MapItem;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class SS_GroupData extends RequestPacket
{
	private Queue<Group> groups;
	private Queue<Vip> vips;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(groups.size());

		while (!groups.isEmpty())
		{
			Group group = groups.poll();

			output.putInt(group.getID());
			output.putInt(group.getAccessLevel());
			output.putString(group.getName());
			output.putByte(b(group.isLogCommands() ? 1 : 0));
			output.putInt(group.getParent() == null ? 0 : group.getParent().getID());

			GroupCommands commands = group.getCommands();
			{
				output.putInt(commands.size());

				for (MapItem<String, Integer> command : commands)
				{
					output.putString(command.key);
					output.putInt(command.value);
				}
			}

			GroupPermissions permissions = group.getPermissions();
			{
				output.putInt(permissions.size());

				for (MapItem<String, Integer> permission : permissions)
				{
					output.putString(permission.key);
					output.putInt(permission.value);
				}
			}
		}

		output.putInt(vips.size());

		while (!vips.isEmpty())
		{
			Vip vip = new Vip();

			output.putInt(vip.getID());
			output.putString(vip.getName());
			output.putByte(vip.getCharSlotCount());
			output.putByte(vip.getCharBilling());
			output.putShort(vip.getMaxStorage());
		}
	}

	@Override
	protected void receiveInput(Input input)
	{
		groups = new DynamicQueue<>();
		vips = new DynamicQueue<>();

		int groupCount = input.getInt();

		for (int i = 0; i < groupCount; i++)
		{
			Group group = new Group();
			group.setID(input.getInt());
			group.setAccessLevel(input.getInt());
			group.setName(input.getString());
			group.setLogCommands(input.getByte() == 1);

			int parent = input.getInt();

			if (parent > 0)
			{
				group.setParent(new Group());
				group.getParent().setID(parent);
			}

			GroupCommands commands = group.getCommands();
			int commandCount = input.getInt();

			for (int c = 0; c < commandCount; c++)
				commands.set(input.getString(), input.getInt());

			GroupPermissions permissions = group.getPermissions();
			int permissionCount = input.getInt();

			for (int c = 0; c < permissionCount; c++)
				permissions.set(input.getString(), input.getInt());

			groups.offer(group);
		}

		int vipCount = input.getInt();

		for (int i = 0; i < vipCount; i++)
		{
			Vip vip = new Vip();
			vip.setID(input.getInt());
			vip.setName(vip.getName());
			vip.setCharSlotCount(vip.getCharSlotCount());
			vip.setCharBilling(vip.getCharBilling());
			vip.setMaxStorage(vip.getMaxStorage());
			vips.offer(vip);
		}
	}

	public Queue<Group> getGroups()
	{
		return groups;
	}

	public void setGroups(Queue<Group> groups)
	{
		this.groups = groups;
	}

	public Queue<Vip> getVips()
	{
		return vips;
	}

	public void setVips(Queue<Vip> vips)
	{
		this.vips = vips;
	}

	@Override
	public String getName()
	{
		return "SS_GROUP_DATA";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SS_GROUP_DATA;
	}

	@Override
	protected int length()
	{
		return DYNAMIC_PACKET_LENGTH;
	}
}

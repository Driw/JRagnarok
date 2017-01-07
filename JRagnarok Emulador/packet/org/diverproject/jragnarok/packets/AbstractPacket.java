package org.diverproject.jragnarok.packets;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.lang.HexUtil;

public abstract class AbstractPacket
{
	public static final int DYNAMIC_PACKET_LENGTH = -1;

	public abstract String getName();
	public abstract short getIdentify();
	protected abstract int length();

	public String getHexIdentify()
	{
		return "0x" +HexUtil.parseInt(getIdentify(), 4);
	}

	protected void toString(ObjectDescription description)
	{
		description.append("id", getIdentify());
		description.append("hex", getHexIdentify());
		description.append("name", getName());
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		toString(description);

		return description.toString();
	}
}

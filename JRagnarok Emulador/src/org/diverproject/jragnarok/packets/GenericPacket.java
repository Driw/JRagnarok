package org.diverproject.jragnarok.packets;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.lang.HexUtil;

public abstract class GenericPacket
{
	public abstract String getName();
	public abstract short getIdentify();

	public String getHexIdentify()
	{
		return HexUtil.parseInt(getIdentify(), 4);
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

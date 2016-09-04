package org.diverproject.jragnarok.server.login.structures;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Node;

public class ClientHashNode extends Node<ClientHash>
{
	private int groupID;

	public ClientHashNode(ClientHash value)
	{
		super(value);
	}

	public int getGroupID()
	{
		return groupID;
	}

	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("groupID", groupID);
		description.append("value", get());
		description.append("prev", getPrev() == null ? null : getPrev().get());
		description.append("next", getNext() == null ? null : getNext().get());

		return description.toString();
	}

}

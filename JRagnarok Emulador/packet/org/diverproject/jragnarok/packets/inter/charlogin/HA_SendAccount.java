package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.JRagnarokUtil.size;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_SEND_ACCOUNTS;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.DynamicList;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_SendAccount extends RequestPacket
{
	private List<Integer> accounts;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accounts.size());

		for (int i = 0; i < accounts.size(); i++)
			output.putInt(accounts.get(i));
	}

	@Override
	protected void receiveInput(Input input)
	{
		accounts = new DynamicList<>();
		int size = input.getInt();

		for (int i = 0; i < size; i++)
			accounts.add(input.getInt());
	}

	public List<Integer> getAccounts()
	{
		return accounts;
	}

	public void setAccounts(List<Integer> accounts)
	{
		this.accounts = accounts;
	}

	@Override
	public String getName()
	{
		return "HA_SEND_ACCOUNTS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_SEND_ACCOUNTS;
	}

	@Override
	protected int length()
	{
		return DYNAMIC_PACKET_LENGTH;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accounts", size(accounts));
	}
}

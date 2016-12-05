package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SEND_ACCOUNTS;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.DynamicList;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class SendAccountRequest extends RequestPacket
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
		return "SEND_ACCOUNTS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SEND_ACCOUNTS;
	}

	@Override
	protected int length()
	{
		return 0;
	}
}

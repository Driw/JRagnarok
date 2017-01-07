package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_SYNCRONIZE_IPADDRESS;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_SyncronizeAddress extends RequestPacket
{
	private int addressIP;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(addressIP);
	}

	@Override
	protected void receiveInput(Input input)
	{
		addressIP = input.getInt();
	}

	public int getAddressIP()
	{
		return addressIP;
	}

	@Override
	public String getName()
	{
		return "AH_SYNCRONIZE_IPADDRESS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_SYNCRONIZE_IPADDRESS;
	}

	@Override
	protected int length()
	{
		return 6;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("addressIP", SocketUtil.socketIP(addressIP));
	}
}

package org.diverproject.jragnarok.packets.receive;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_EXE_HASHCHECK;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class UpdateClientHash extends ReceivePacket
{
	private byte hashValue[];

	public UpdateClientHash()
	{
		hashValue = new byte[16];
	}

	@Override
	protected void receiveInput(Input input)
	{
		input.getBytes(hashValue);
	}

	public byte[] getHashValue()
	{
		return hashValue;
	}

	@Override
	public String getName()
	{
		return "PACKET_CA_EXE_HASHCHECK";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_EXE_HASHCHECK;
	}

	@Override
	protected int length()
	{
		return 16;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("hashValue", new String(hashValue));
	}
}

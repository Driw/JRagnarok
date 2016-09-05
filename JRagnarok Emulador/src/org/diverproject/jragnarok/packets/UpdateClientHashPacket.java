package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_EXE_HASHCHECK;

import org.diverproject.util.stream.implementation.input.InputPacket;

public class UpdateClientHashPacket extends ReceivePacket
{
	private byte hashValue[];

	public UpdateClientHashPacket()
	{
		hashValue = new byte[16];
	}

	@Override
	protected void receiveInput(InputPacket input)
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
}

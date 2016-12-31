package org.diverproject.jragnarok.packets.login.client_acess;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_EXE_HASHCHECK;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CA_ExeHashCheck extends ReceivePacket
{
	private byte hashValue[];

	@Override
	protected void receiveInput(Input input)
	{
		input.getBytes((hashValue = new byte[16]));
	}

	public byte[] getHashValue()
	{
		return hashValue;
	}

	@Override
	public String getName()
	{
		return "CA_EXE_HASHCHECK";
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

		description.append("hashValue", hashValue);
	}
}

package org.diverproject.jragnarok.packets;

import org.diverproject.util.lang.HexUtil;
import org.diverproject.util.stream.Input;

public class AcknowledgePacket extends ReceivePacket
{
	private short expectedPacket;
	private short packetID;

	public AcknowledgePacket()
	{
		this(0);
	}

	public AcknowledgePacket(int expectedPacket)
	{
		if (this.expectedPacket > Short.MAX_VALUE * 2)
			throw new UnknowPacketException("pacote %s inválido", HexUtil.parseInt(expectedPacket, 4));

		this.expectedPacket = (short) expectedPacket;
	}

	@Override
	protected void receiveInput(Input input)
	{
		packetID = input.getShort();

		if (expectedPacket > 0 && packetID != expectedPacket)
			throw new UnknowPacketException("esperado %s e recebido %s", getHexIdentify(), HexUtil.parseInt(packetID, 4));
	}

	public short getPacketID()
	{
		return packetID;
	}

	@Override
	public String getName()
	{
		return "ReceivePacketID";
	}

	@Override
	public short getIdentify()
	{
		return expectedPacket;
	}

	@Override
	protected int length()
	{
		return 4;
	}
}

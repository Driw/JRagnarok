package org.diverproject.jragnarok.packets;

import org.diverproject.util.lang.HexUtil;
import org.diverproject.util.stream.implementation.input.InputPacket;

public class ReceivePacketIDPacket extends ReceivePacket
{
	private short expectedPacket;
	private short packetID;

	public ReceivePacketIDPacket()
	{
		this(0);
	}

	public ReceivePacketIDPacket(int expectedPacket)
	{
		if (this.expectedPacket > Short.MAX_VALUE * 2)
			throw new UnknowPacketException("pacote %s inválido", HexUtil.parseInt(expectedPacket, 4));

		this.expectedPacket = (short) expectedPacket;
	}

	@Override
	protected void receiveInput(InputPacket input)
	{
		packetID = input.getShort();

		if (expectedPacket > 0 && packetID != expectedPacket)
			throw new UnknowPacketException("esperado %s e recebido %s", getHexIdentify(), HexUtil.parseInt(packetID, 4));

		input.skipe(-2);
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
}

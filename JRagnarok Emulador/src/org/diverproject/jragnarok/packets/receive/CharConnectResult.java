package org.diverproject.jragnarok.packets.receive;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_RES_CHAR_CONNECT;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CharConnectResult extends ReceivePacket
{
	private AuthResult result;

	@Override
	protected void receiveInput(Input input)
	{
		result = AuthResult.parse(input.getByte());
	}

	public AuthResult getResult()
	{
		return result;
	}

	@Override
	public String getName()
	{
		return "PACKET_CA_RES_CHAR_CONNECT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_RES_CHAR_CONNECT;
	}

	@Override
	protected int length()
	{
		return 1;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("result", result);
	}
}

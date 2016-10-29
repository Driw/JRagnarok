package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_RES_CHAR_CONNECT;

import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class CharConnectResult extends ResponsePacket
{
	private AuthResult result;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result.CODE);
	}

	public void setResult(AuthResult result)
	{
		this.result = result;
	}

	@Override
	public String getName()
	{
		return "PACKET_CA_RES_CHAR_CONNECT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_RES_CHAR_CONNECT;
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

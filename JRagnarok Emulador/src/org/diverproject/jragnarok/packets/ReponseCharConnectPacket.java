package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_RES_CHAR_CONNECT;

import org.diverproject.jragnarok.server.login.services.AuthResult;
import org.diverproject.util.stream.implementation.output.OutputPacket;

public class ReponseCharConnectPacket extends ResponsePacket
{
	private AuthResult result;

	@Override
	protected void sendOutput(OutputPacket output)
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
}

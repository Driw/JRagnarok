package org.diverproject.jragnarok.packets.request;


import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_CHAR_SERVER_CONNECT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class CharServerConnectResult extends RequestPacket
{
	private AuthResult result;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result.CODE);
	}

	@Override
	protected void receiveInput(Input input)
	{
		result = AuthResult.parse(input.getByte());
	}

	public AuthResult getResult()
	{
		return result;
	}

	public void setResult(AuthResult result)
	{
		this.result = result;
	}

	@Override
	public String getName()
	{
		return "RES_CHAR_SERVER_CONNECT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_RES_CHAR_SERVER_CONNECT;
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

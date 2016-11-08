package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_REFUSE_ENTER;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class RefuseEnter extends ResponsePacket
{
	private AuthResult result;

	public RefuseEnter()
	{
	}

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
		return "PACKET_REFUSE_ENTER";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REFUSE_ENTER;
	}

	@Override
	protected int length()
	{
		return 21;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("code", result);
	}
}

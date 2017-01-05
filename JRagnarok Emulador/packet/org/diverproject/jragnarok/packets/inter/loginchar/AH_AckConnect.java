package org.diverproject.jragnarok.packets.inter.loginchar;


import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_ACK_CONNECT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.packets.common.RefuseLogin;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_AckConnect extends RequestPacket
{
	private RefuseLogin result;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result.CODE);
	}

	@Override
	protected void receiveInput(Input input)
	{
		result = RefuseLogin.parse(input.getByte());
	}

	public RefuseLogin getResult()
	{
		return result;
	}

	public void setResult(RefuseLogin result)
	{
		this.result = result;
	}

	@Override
	public String getName()
	{
		return "AH_CHAR_SERVER_CONNECT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_ACK_CONNECT;
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

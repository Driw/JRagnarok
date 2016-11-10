package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REFUSE_ENTER;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class RefuseEnter extends ResponsePacket
{
	public static final byte REJECTED_FROM_SERVER = 0;
	public static final String CODE_STRINGS[] = new String[]
	{
		"REJECTED_FROM_SERVER"
	};

	private byte result;

	public RefuseEnter()
	{
	}

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result);
	}

	public void setResult(byte result)
	{
		this.result = result;
	}

	@Override
	public String getName()
	{
		return "REFUSE_ENTER";
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

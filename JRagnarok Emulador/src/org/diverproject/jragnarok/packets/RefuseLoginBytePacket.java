package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_AC_REFUSE_LOGIN;

import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class RefuseLoginBytePacket extends ResponsePacket
{
	private AuthResult result;
	private String blockDate;

	public RefuseLoginBytePacket()
	{
		blockDate = "";
	}

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result.CODE);
		output.putString(blockDate, 20);
	}

	public void setResult(AuthResult result)
	{
		this.result = result;
	}

	public void setBlockDate(String blockDate)
	{
		if (blockDate != null)
			this.blockDate = strcap(blockDate, 20);
	}

	@Override
	public String getName()
	{
		return "PACKET_AC_REFUSE_LOGIN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AC_REFUSE_LOGIN;
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
		description.append("blockDate", blockDate);
	}
}

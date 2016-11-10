package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REFUSE_LOGIN;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class RefuseLoginByte extends ResponsePacket
{
	private AuthResult result;
	private String blockDate;

	public RefuseLoginByte()
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
		return "REFUSE_LOGIN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REFUSE_LOGIN;
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
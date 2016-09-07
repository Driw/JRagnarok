package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_AC_REFUSE_LOGIN;

import org.diverproject.jragnarok.server.login.services.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.implementation.output.OutputPacket;

public class RefuseLoginPacket extends ResponsePacket
{
	private AuthResult result;
	private String blockDate;

	public RefuseLoginPacket()
	{
		blockDate = "";
	}

	@Override
	protected void sendOutput(OutputPacket output)
	{
		output.putByte(result.CODE);
		output.putBytes(blockDate.getBytes());
		output.skipe(20 - blockDate.length());
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
	protected void toString(ObjectDescription description)
	{
		description.append("code", result);
		description.append("blockDate", blockDate);
	}
}

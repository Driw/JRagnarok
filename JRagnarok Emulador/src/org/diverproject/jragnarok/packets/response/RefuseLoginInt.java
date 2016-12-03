package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REFUSE_LOGIN_R2;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.common.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class RefuseLoginInt extends ResponsePacket
{
	private AuthResult result;
	private String blockDate;

	public RefuseLoginInt()
	{
		blockDate = "";
	}

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(result.CODE);
		output.putString(blockDate, 20);
	}

	@Override
	public String getName()
	{
		return "REFUSE_LOGIN_R2";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REFUSE_LOGIN_R2;
	}

	public void setResult(AuthResult result)
	{
		this.result = result;
	}

	public String getBlockDate()
	{
		return blockDate;
	}

	public void setBlockDate(String blockDate)
	{
		if (blockDate != null)
			this.blockDate = strcap(blockDate, 20);
	}

	@Override
	protected int length()
	{
		return 24;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append(result);
		description.append(blockDate);
	}
}

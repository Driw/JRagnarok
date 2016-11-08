package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.JRagnarokUtil.strcap;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_REFUSE_LOGIN_R2;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class RefuseLoginInt extends ResponsePacket
{
	public static final byte REJECTED_FROM_SERVER = 3;
	public static final String CODE_STRINGS[] = new String[]
	{
		"", "", "REJECTED_FROM_SERVER"
	};

	private int code;
	private String blockDate;

	public RefuseLoginInt()
	{
		blockDate = "";
	}

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(code);
		output.putString(blockDate, 20);
	}

	@Override
	public String getName()
	{
		return "PACKET_REFUSE_LOGIN_R2";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REFUSE_LOGIN_R2;
	}

	public void setCode(AuthResult result)
	{
		this.code = result.CODE;
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

		description.append(CODE_STRINGS[code]);
		description.append(blockDate);
	}
}

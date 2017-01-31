package org.diverproject.jragnarok.packets.login.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AC_REFUSE_LOGIN;
import static org.diverproject.util.Util.strcap;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.common.RefuseLogin;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class AC_RefuseLogin extends ResponsePacket
{
	private RefuseLogin result;
	private String blockDate;

	public AC_RefuseLogin()
	{
		blockDate = "";
	}

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result.CODE);
		output.putString(blockDate, 20);
	}

	public void setResult(RefuseLogin result)
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
		return "AC_REFUSE_LOGIN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AC_REFUSE_LOGIN;
	}

	@Override
	protected int length()
	{
		return 23;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("code", result);
		description.append("blockDate", blockDate);
	}
}

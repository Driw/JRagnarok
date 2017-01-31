package org.diverproject.jragnarok.packets.login.toclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AC_ACK_HASH;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.Util.strcap;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class AC_AckHash extends ResponsePacket
{
	private short md5KeyLength;
	private String md5Key;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(s(md5KeyLength + 4));
		output.putString(md5Key, md5KeyLength);
	}

	public void setMD5KeyLength(short md5KeyLength)
	{
		if (md5KeyLength > 0)
			this.md5KeyLength = md5KeyLength;
	}

	public void setMD5Key(String md5Key)
	{
		this.md5Key = strcap(md5Key, md5KeyLength);
	}

	@Override
	public String getName()
	{
		return "AC_ACK_HASH";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AC_ACK_HASH;
	}

	@Override
	protected int length()
	{
		return 4 + md5KeyLength;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("md5KeyLength", md5KeyLength);
		description.append("md5Key", md5Key);
	}
}

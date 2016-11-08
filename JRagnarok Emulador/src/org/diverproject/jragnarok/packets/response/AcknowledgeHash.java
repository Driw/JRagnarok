package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_ACKNOWLEDGE_HASH;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class AcknowledgeHash extends ResponsePacket
{
	private short md5KeyLength;
	private String md5Key;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort((short) (md5KeyLength + 4));
		output.putString(md5Key, 20);
	}

	public void setMD5KeyLength(short md5KeyLength)
	{
		this.md5KeyLength = md5KeyLength;
	}

	public void setMD5Key(String md5Key)
	{
		this.md5Key = md5Key;
	}

	@Override
	public String getName()
	{
		return "PACKET_AC_ACK_HASH";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ACKNOWLEDGE_HASH;
	}

	@Override
	protected int length()
	{
		return 22;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("md5KeyLength", md5KeyLength);
		description.append("md5Key", md5Key);
	}
}

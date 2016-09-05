package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_AC_ACK_HASH;

import org.diverproject.util.stream.implementation.output.OutputPacket;

public class AcknologeHash extends ResponsePacket
{
	private short md5KeyLength;
	private String md5Key;

	@Override
	protected void sendOutput(OutputPacket output)
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
		return PACKET_AC_ACK_HASH;
	}
}

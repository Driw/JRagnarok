package org.diverproject.jragnarok.packets;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_AC_REFUSE_LOGIN;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.implementation.output.OutputPacket;

public class RefuseLoginPacket extends ResponsePacket
{
	public static final byte REJECTED_FROM_SERVER = 3;
	public static final String CODE_STRINGS[] = new String[]
	{
		"", "", "REJECTED_FROM_SERVER"
	};

	private byte code;
	private String blockDate;

	public RefuseLoginPacket()
	{
		blockDate = "";
	}

	@Override
	protected void sendOutput(OutputPacket output)
	{
		output.putByte(code);
		output.putBytes(blockDate.getBytes());
		output.skipe(20 - blockDate.length());
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

	public byte getCode()
	{
		return code;
	}

	public void setCode(byte code)
	{
		this.code = code;
	}

	public String getBlockDate()
	{
		return blockDate;
	}

	public void setBlockDate(String blockDate)
	{
		if (blockDate != null)
			this.blockDate = blockDate.length() > 20 ? blockDate.substring(0, 20) : blockDate;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		description.append(CODE_STRINGS[code]);
		description.append(blockDate);
	}
}

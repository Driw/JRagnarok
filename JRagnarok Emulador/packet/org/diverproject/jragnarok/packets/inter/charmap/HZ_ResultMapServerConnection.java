package org.diverproject.jragnarok.packets.inter.charmap;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HZ_RESULT_MAP_CONNECTION;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.jragnarok.packets.common.ResultMapServerConnection;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HZ_ResultMapServerConnection extends RequestPacket
{
	private ResultMapServerConnection result;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(result.CODE);
	}

	@Override
	protected void receiveInput(Input input)
	{
		result = ResultMapServerConnection.parse(input.getByte());
	}

	public ResultMapServerConnection getResult()
	{
		return result;
	}

	public void setResult(ResultMapServerConnection result)
	{
		this.result = result;
	}

	@Override
	public String getName()
	{
		return "HZ_RESULT_MAP_CONNECTION";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HZ_RESULT_MAP_CONNECTION;
	}

	@Override
	protected int length()
	{
		return 3;
	}
}

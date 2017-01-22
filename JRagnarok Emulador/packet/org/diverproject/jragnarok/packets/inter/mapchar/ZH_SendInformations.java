package org.diverproject.jragnarok.packets.inter.mapchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ZH_SEND_INFORMATIONS;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class ZH_SendInformations extends RequestPacket
{
	private int baseRate;
	private int jobRate;
	private int dropRate;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(baseRate);
		output.putInt(jobRate);
		output.putInt(dropRate);
	}

	@Override
	protected void receiveInput(Input input)
	{
		baseRate = input.getInt();
		jobRate = input.getInt();
		dropRate = input.getInt();
	}

	public int getBaseRate()
	{
		return baseRate;
	}

	public void setBaseRate(int baseRate)
	{
		this.baseRate = baseRate;
	}

	public int getJobRate()
	{
		return jobRate;
	}

	public void setJobRate(int jobRate)
	{
		this.jobRate = jobRate;
	}

	public int getDropRate()
	{
		return dropRate;
	}

	public void setDropRate(int dropRate)
	{
		this.dropRate = dropRate;
	}

	@Override
	public String getName()
	{
		return "ZC_SEND_INFORMATIONS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ZH_SEND_INFORMATIONS;
	}

	@Override
	protected int length()
	{
		return 14;
	}
}

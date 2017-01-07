package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_ACCEPT2;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class HC_Accept2 extends ResponsePacket
{
	private byte normalSlots;
	private byte premiumSlots;
	private byte billingSlots;
	private byte producibleSlots;
	private byte validSlots;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(s(length()));
		output.putByte(normalSlots);
		output.putByte(premiumSlots);
		output.putByte(billingSlots);
		output.putByte(producibleSlots);
		output.putByte(validSlots);
		output.skipe(20);
		System.out.println(this);
	}

	public void setNormalSlots(byte normalSlots)
	{
		this.normalSlots = normalSlots;
	}

	public void setPremiumSlots(byte premiumSlots)
	{
		this.premiumSlots = premiumSlots;
	}

	public void setBillingSlots(byte billingSlots)
	{
		this.billingSlots = billingSlots;
	}

	public void setProducibleSlots(byte producibleSlots)
	{
		this.producibleSlots = producibleSlots;
	}

	public void setValidSlots(byte validSlots)
	{
		this.validSlots = validSlots;
	}

	@Override
	public String getName()
	{
		return "SEND_ACCOUNT_SLOT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_ACCEPT2;
	}

	@Override
	protected int length()
	{
		return 29;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("normalSlots", normalSlots);
		description.append("premiumSlots", premiumSlots);
		description.append("billingSlots", billingSlots);
		description.append("producibleSlots", producibleSlots);
		description.append("validSlots", validSlots);
	}
}

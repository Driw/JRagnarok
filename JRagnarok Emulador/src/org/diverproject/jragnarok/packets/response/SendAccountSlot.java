package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SEND_ACCOUNT_SLOT;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.stream.Output;

public class SendAccountSlot extends ResponsePacket
{
	private byte minChars;
	private byte charsVip;
	private byte charsBilling;
	private byte charsSlot;
	private byte maxChars;

	@Override
	protected void sendOutput(Output output)
	{
		output.putByte(b(29)); // TODO what is that?
		output.putByte(minChars);
		output.putByte(charsVip);
		output.putByte(charsBilling);
		output.putByte(charsSlot);
		output.putByte(maxChars);
		output.skipe(20);
	}

	public void setMinChars(byte minChars)
	{
		this.minChars = minChars;
	}

	public void setCharsVip(byte charsVip)
	{
		this.charsVip = charsVip;
	}

	public void setCharsBilling(byte charsBilling)
	{
		this.charsBilling = charsBilling;
	}

	public void setCharsSlot(byte charsSlot)
	{
		this.charsSlot = charsSlot;
	}

	public void setMaxChars(byte maxChars)
	{
		this.maxChars = maxChars;
	}

	@Override
	public String getName()
	{
		return "SEND_ACCOUNT_SLOT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_SEND_ACCOUNT_SLOT;
	}

	@Override
	protected int length()
	{
		return 27;
	}
}

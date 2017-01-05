package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_CHARS;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_ACCEPT_ENTER_NEO_UNION;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.character.entities.Character;
import org.diverproject.util.collection.Index;
import org.diverproject.util.stream.Output;

public class HC_AcceptEnterNeoUnion extends ResponsePacket
{
	private boolean charMoveEnabled;
	private boolean charMoveUnlimited;
	private int charMoveCount;

	private byte totalSlots;
	private byte premiumStartSlot;
	private byte premiumEndSlot;
	private byte dummyBeginBilling;
	private int code;
	private int firstTime;
	private int secondTime;
	private String dummyEndBilling;
	private Index<Character> characters;

	@Override
	protected void sendOutput(Output output)
	{
		output.putShort(s(length() + 2));
		output.putByte(totalSlots);
		output.putByte(premiumStartSlot);
		output.putByte(premiumEndSlot);
		output.putByte(dummyBeginBilling);
		output.putInt(code);
		output.putInt(firstTime);
		output.putInt(secondTime);
		output.putString(dummyEndBilling, 7);

		for (int slot = 0; slot < MAX_CHARS; slot++)
			if (characters.get(slot) != null)
				CharacterInfo.put(output, slot, characters.get(slot), charMoveEnabled, charMoveUnlimited, charMoveCount);
	}

	public void setCharMoveEnabled(boolean charMoveEnabled)
	{
		this.charMoveEnabled = charMoveEnabled;
	}

	public void setCharMoveUnlimited(boolean charMoveUnlimited)
	{
		this.charMoveUnlimited = charMoveUnlimited;
	}

	public void setCharMoveCount(int charMoveCount)
	{
		this.charMoveCount = charMoveCount;
	}

	public void setTotalSlots(byte totalSlots)
	{
		this.totalSlots = totalSlots;
	}

	public void setPremiumStartSlot(byte premiumStartSlot)
	{
		this.premiumStartSlot = premiumStartSlot;
	}

	public void setPremiumEndSlot(byte premiumEndSlot)
	{
		this.premiumEndSlot = premiumEndSlot;
	}

	public void setDummyBeginBilling(byte dummyBeginBilling)
	{
		this.dummyBeginBilling = dummyBeginBilling;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public void setFirstTime(int firstTime)
	{
		this.firstTime = firstTime;
	}

	public void setSecondTime(int secondTime)
	{
		this.secondTime = secondTime;
	}

	public void setDummyEndBilling(String dummyEndBilling)
	{
		if (dummyEndBilling == null)
			dummyEndBilling = "";

		this.dummyEndBilling = dummyEndBilling;
	}

	public void setCharacters(Index<Character> characters)
	{
		this.characters = characters;
	}

	@Override
	public String getName()
	{
		return "SEND_ACCOUNT_CHARS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_ACCEPT_ENTER_NEO_UNION;
	}

	@Override
	protected int length()
	{
		return 25 + Character.BYTES * characters.size();
	}
}

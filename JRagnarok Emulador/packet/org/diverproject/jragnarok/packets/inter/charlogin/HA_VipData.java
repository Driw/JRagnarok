package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_VIP_DATA;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_VipData extends RequestPacket
{
	public static final byte VIP_DATA_NONE = 0x0;
	public static final byte VIP_DATA_VIP = 0x1;
	public static final byte VIP_DATA_GM = 0x2;
	public static final byte VIP_DATA_SHOW_RATES = 0x4;
	public static final byte VIP_DATA_FORCE = 0x8;
	public static final String VIP_DATA_STRINGS[] = new String[] { "VIP", "GM", "SHOW_RATES", "FORCE" };

	private int accountID;
	private int vipDuration;
	private int mapFD;
	private byte flag;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInts(accountID, vipDuration, mapFD);
		output.putByte(flag);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		vipDuration = input.getInt();
		mapFD = input.getInt();
		flag = input.getByte();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public int getVipDuration()
	{
		return vipDuration;
	}

	public void setVipDuration(int vipDuration)
	{
		this.vipDuration = vipDuration;
	}

	public int getMapFD()
	{
		return mapFD;
	}

	public void setMapFD(int mapFD)
	{
		this.mapFD = mapFD;
	}

	public byte getFlag()
	{
		return flag;
	}

	public void setFlag(byte flag)
	{
		this.flag = flag;
	}

	@Override
	public String getName()
	{
		return "HA_VIP_DATA";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_VIP_DATA;
	}

	@Override
	protected int length()
	{
		return 15;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("vipDuration", vipDuration);
		description.append("mapFD", mapFD);
		description.append("flag", flag);
	}
}

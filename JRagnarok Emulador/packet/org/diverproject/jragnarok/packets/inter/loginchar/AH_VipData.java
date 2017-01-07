package org.diverproject.jragnarok.packets.inter.loginchar;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_VIP_DATA;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AH_VipData extends RequestPacket
{
	private int accountID;
	private long vipTimeout;
	private int vipGroupID;
	private byte vipFlag;
	private int mapFD;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putLong(vipTimeout);
		output.putInt(vipGroupID);
		output.putByte(vipFlag);
		output.putInt(mapFD);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		vipTimeout = input.getLong();
		vipGroupID = input.getInt();
		vipFlag = input.getByte();
		mapFD = input.getInt();
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public long getVipTimeout()
	{
		return vipTimeout;
	}

	public void setVipTimeout(long vipTimeout)
	{
		this.vipTimeout = vipTimeout;
	}

	public byte getVipFlag()
	{
		return vipFlag;
	}

	public void setVipFlag(byte vipFlag)
	{
		this.vipFlag = vipFlag;
	}

	public int getVipGroupID()
	{
		return vipGroupID;
	}

	public void setVipGroupID(int vipGroupID)
	{
		this.vipGroupID = vipGroupID;
	}

	public int getMapFD()
	{
		return mapFD;
	}

	public void setMapFD(int mapFD)
	{
		this.mapFD = mapFD;
	}

	@Override
	public String getName()
	{
		return "AH_VIP_DATA";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AH_VIP_DATA;
	}

	@Override
	protected int length()
	{
		return 20;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", accountID);
		description.append("vipTimeout", vipTimeout);
		description.append("vipGroupID", vipGroupID);
		description.append("mapFD", mapFD);
		description.append("vipFlag", vipFlag);
	}
}

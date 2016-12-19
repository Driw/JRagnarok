package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_VIP_DATA;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class VipDataResult extends RequestPacket
{
	private int accountID;
	private int groupID;
	private int mapFD;
	private long vipTimeout;
	private byte flag;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(accountID);
		output.putInt(groupID);
		output.putInt(mapFD);
		output.putLong(vipTimeout);
		output.putByte(flag);
	}

	@Override
	protected void receiveInput(Input input)
	{
		accountID = input.getInt();
		groupID = input.getInt();
		mapFD = input.getInt();
		vipTimeout = input.getLong();
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

	public long getVipTimeout()
	{
		return vipTimeout;
	}

	public void setVipTimeout(long vipTimeout)
	{
		this.vipTimeout = vipTimeout;
	}

	public int getGroupID()
	{
		return groupID;
	}

	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
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
		return "RES_VIP_DATA";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_RES_VIP_DATA;
	}

	@Override
	protected int length()
	{
		return 17;
	}
}

package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_ACCOUNT_INFO;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class AccountInfoRequest extends RequestPacket
{
	private int mapfd;
	private int ufd;
	private int aid;
	private int groupID;
	private int accountID;
	private boolean useData;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(mapfd);
		output.putInt(ufd);
		output.putInt(aid);
		output.putInt(groupID);
		output.putInt(accountID);
		output.putByte(b(useData ? 1 : 0));
	}

	@Override
	protected void receiveInput(Input input)
	{
		mapfd = input.getInt();
		ufd = input.getInt();
		aid = input.getInt();
		groupID = input.getInt();
		accountID = input.getInt();
		useData = input.getByte() == 1;
	}

	public int getMapFD()
	{
		return mapfd;
	}

	public void setMapFD(int mapfd)
	{
		if (mapfd > 0)
			this.mapfd = mapfd;
	}

	public int getUFD()
	{
		return ufd;
	}

	public void setUFD(int ufd)
	{
		if (ufd > 0)
			this.ufd = ufd;
	}

	public int getAID()
	{
		return aid;
	}

	public void setAID(int aid)
	{
		if (aid > 0)
			this.aid = aid;
	}

	public int getGroupID()
	{
		return groupID;
	}

	public void setGroupID(int groupID)
	{
		if (groupID > 0)
			this.groupID = groupID;
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		if (accountID > 0)
			this.accountID = accountID;
	}

	public boolean hasData()
	{
		return useData;
	}

	public void setData(boolean use)
	{
		this.useData = use;
	}

	@Override
	public String getName()
	{
		return "REQ_ACCOUNT_INFO";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REQ_ACCOUNT_INFO;
	}

	@Override
	protected int length()
	{
		return 21;
	}
}

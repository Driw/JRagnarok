package org.diverproject.jragnarok.packets.character.toclient;

import static org.diverproject.jragnarok.JRagnarokConstants.MAP_NAME_LENGTH_EXT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HC_NOTIFY_ZONESVR;
import static org.diverproject.util.Util.strcap;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.lang.Bits;
import org.diverproject.util.stream.Output;

public class HC_NotifyZoneServer extends ResponsePacket
{
	private int charID;
	private String mapName;
	private int addressIP;
	private short port;

	@Override
	protected void sendOutput(Output output)
	{
		output.putInt(charID);
		output.putString(mapName, MAP_NAME_LENGTH_EXT);
		output.putInt(Bits.swap(addressIP));
		output.putShort(port);
	}

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	public void setMapName(String mapName)
	{
		this.mapName = strcap(mapName, MAP_NAME_LENGTH_EXT);
	}

	public void setAddressIP(int addressIP)
	{
		this.addressIP = addressIP;
	}

	public void setPort(short port)
	{
		this.port = port;
	}

	@Override
	public String getName()
	{
		return "HC_NOTIFY_ZONESVR";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HC_NOTIFY_ZONESVR;
	}

	@Override
	protected int length()
	{
		return 28;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("accountID", charID);
		description.append("mapName", mapName);
		description.append("addressIP", addressIP);
		description.append("port", port);
	}
}

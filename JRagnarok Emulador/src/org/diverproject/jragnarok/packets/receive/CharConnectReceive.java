package org.diverproject.jragnarok.packets.receive;

import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_REQ_CHAR_CONNECT;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CharConnectReceive extends ReceivePacket
{
	private String username;
	private String password;
	private int serverIP;
	private short serverPort;
	private String serverName;
	private short type;
	private short newDisplay;

	@Override
	protected void receiveInput(Input input)
	{
		username = strclr(input.getString(24));
		password = strclr(input.getString(24));
		input.skipe(4);
		serverIP = input.getInt();
		serverPort = input.getShort();
		serverName = strclr(input.getString(20));
		input.skipe(2);
		type = input.getShort();
		newDisplay = input.getShort();
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public int getServerIP()
	{
		return serverIP;
	}

	public short getServerPort()
	{
		return serverPort;
	}

	public String getServerName()
	{
		return serverName;
	}

	public short getType()
	{
		return type;
	}

	public short getNewDisplay()
	{
		return newDisplay;
	}

	@Override
	public String getName()
	{
		return "PACKET_CA_REQ_CHAR_CONNECT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_REQ_CHAR_CONNECT;
	}

	@Override
	protected int length()
	{
		return 84;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("username", username);
		description.append("password", password);
		description.append("serverIP", serverIP);
		description.append("serverPort", serverPort);
		description.append("serverName", serverName);
		description.append("serverType", type);
		description.append("newDisplay", newDisplay);
	}
}

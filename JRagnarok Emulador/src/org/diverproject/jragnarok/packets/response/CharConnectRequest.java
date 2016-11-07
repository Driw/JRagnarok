package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_CA_REQ_CHAR_CONNECT;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Output;

public class CharConnectRequest extends ResponsePacket
{
	private String username;
	private String password;
	private int serverIP;
	private short serverPort;
	private String serverName;
	private short type;
	private short newDisplay;

	@Override
	protected void sendOutput(Output output)
	{
		output.putString(username, 24);
		output.putString(password, 24);
		output.skipe(4);
		output.putInt(serverIP);
		output.putShort(serverPort);
		output.putString(serverName, 20);
		output.skipe(2);
		output.putShort(type);
		output.putShort(newDisplay);
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setServerIP(int serverIP)
	{
		this.serverIP = serverIP;
	}

	public void setServerPort(short serverPort)
	{
		this.serverPort = serverPort;
	}

	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	public void setType(short type)
	{
		this.type = type;
	}

	public void setNewDisplay(short newValue)
	{
		this.newDisplay = newValue;
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

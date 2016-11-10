package org.diverproject.jragnarok.packets.request;

import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_CHAR_SERVER_CONNECT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class CharServerConnectRequest extends RequestPacket
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

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public int getServerIP()
	{
		return serverIP;
	}

	public void setServerIP(int serverIP)
	{
		this.serverIP = serverIP;
	}

	public short getServerPort()
	{
		return serverPort;
	}

	public void setServerPort(short serverPort)
	{
		this.serverPort = serverPort;
	}

	public String getServerName()
	{
		return serverName;
	}

	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	public short getType()
	{
		return type;
	}

	public void setType(short type)
	{
		this.type = type;
	}

	public short getNewDisplay()
	{
		return newDisplay;
	}

	public void setNewDisplay(short newDisplay)
	{
		this.newDisplay = newDisplay;
	}

	@Override
	public String getName()
	{
		return "REQ_CHAR_SERVER_CONNECT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_REQ_CHAR_SERVER_CONNECT;
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

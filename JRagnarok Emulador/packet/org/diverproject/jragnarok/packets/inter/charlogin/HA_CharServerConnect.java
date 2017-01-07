package org.diverproject.jragnarok.packets.inter.charlogin;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_CHARSERVERCONNECT;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class HA_CharServerConnect extends RequestPacket
{
	private String username;
	private String password;
	private int serverIP;
	private short serverPort;
	private String serverName;
	private short type;
	private boolean newDisplay;

	@Override
	protected void sendOutput(Output output)
	{
		output.putString(username, 24);
		output.putString(password, 24);
		output.putInt(serverIP);
		output.putShort(serverPort);
		output.putString(serverName, 20);
		output.putShort(type);
		output.putShort(s(newDisplay ? 1 : 0));
	}

	@Override
	protected void receiveInput(Input input)
	{
		username = strclr(input.getString(24));
		password = strclr(input.getString(24));
		serverIP = input.getInt();
		serverPort = input.getShort();
		serverName = strclr(input.getString(20));
		type = input.getShort();
		newDisplay = input.getShort() == 1;
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

	public boolean getNewDisplay()
	{
		return newDisplay;
	}

	public void setNewDisplay(boolean newDisplay)
	{
		this.newDisplay = newDisplay;
	}

	@Override
	public String getName()
	{
		return "CA_CHARSERVERCONNECT";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_HA_CHARSERVERCONNECT;
	}

	@Override
	protected int length()
	{
		return 80;
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

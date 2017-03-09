package org.diverproject.jragnarok.packets.inter.mapchar;

import static org.diverproject.jragnarok.JRagnarokConstants.PASSWORD_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.USERNAME_LENGTH;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ZH_MAP_SERVER_CONNECTION;
import static org.diverproject.util.Util.strclr;

import org.diverproject.jragnarok.packets.RequestPacket;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;
import org.diverproject.util.stream.Output;

public class ZH_MapServerConnection extends RequestPacket
{
	private String username;
	private String password;
	private int ipAddress;
	private short port;

	@Override
	protected void sendOutput(Output output)
	{
		output.putString(username, USERNAME_LENGTH);
		output.putString(password, PASSWORD_LENGTH);
		output.putInt(ipAddress);
		output.putShort(port);
	}

	@Override
	protected void receiveInput(Input input)
	{
		username = strclr(input.getString(USERNAME_LENGTH));
		password = strclr(input.getString(PASSWORD_LENGTH));
		ipAddress = input.getInt();
		port = input.getShort();
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

	public int getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress(int ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	public short getPort()
	{
		return port;
	}

	public void setPort(short port)
	{
		this.port = port;
	}

	@Override
	public String getName()
	{
		return "ZC_MAP_SERVER_CONNECTION";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_ZH_MAP_SERVER_CONNECTION;
	}

	@Override
	protected int length()
	{
		return 56;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("username", username);
		description.append("password", password);
		description.append("ipAddress", ipAddress);
		description.append("port", port);
	}
}

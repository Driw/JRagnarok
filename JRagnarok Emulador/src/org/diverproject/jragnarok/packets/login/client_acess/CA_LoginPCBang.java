package org.diverproject.jragnarok.packets.login.client_acess;

import static org.diverproject.jragnarok.JRagnarokUtil.strclr;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN_PCBANG;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CA_LoginPCBang extends ReceivePacket
{
	private int version;
	private String username;
	private String password;
	private ClientType clientType;
	private String ip;
	private String macAddress;

	@Override
	protected void receiveInput(Input input)
	{
		version = input.getInt();
		username = strclr(input.getString(24));
		password = strclr(input.getString(16));
		clientType = ClientType.parse(input.getByte());
		ip = strclr(input.getString(16));
		macAddress = strclr(input.getString(13));
	}

	public int getVersion()
	{
		return version;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public ClientType getClientType()
	{
		return clientType;
	}

	public String getIp()
	{
		return ip;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	@Override
	public String getName()
	{
		return "CA_LOGIN_PCBANG";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_LOGIN_PCBANG;
	}

	@Override
	protected int length()
	{
		return 58;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("version", version);
		description.append("username", username);
		description.append("password", password);
		description.append("clientType", clientType);
		description.append("ip", ip);
		description.append("macAddress", macAddress);
	}
}

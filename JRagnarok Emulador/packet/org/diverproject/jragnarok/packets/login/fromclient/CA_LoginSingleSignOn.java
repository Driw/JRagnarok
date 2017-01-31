package org.diverproject.jragnarok.packets.login.fromclient;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_SSO_LOGIN_REQ;
import static org.diverproject.util.Util.strclr;

import org.diverproject.jragnarok.packets.ReceivePacket;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.stream.Input;

public class CA_LoginSingleSignOn extends ReceivePacket
{
	private int version;
	private ClientType clientType;
	private String username;
	private String password;
	private String macAddress;
	private String ip;
	private String token;

	@Override
	protected void receiveInput(Input input)
	{
		short tokeLength = input.getShort();

		version = input.getInt();
		clientType = ClientType.parse(input.getByte());
		username = strclr(input.getString(24));
		password = strclr(input.getString(27));
		macAddress = strclr(input.getString(17));
		ip = strclr(input.getString(15));
		token = strclr(input.getString(tokeLength));
	}

	public int getVersion()
	{
		return version;
	}

	public ClientType getClientType()
	{
		return clientType;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	public String getIp()
	{
		return ip;
	}

	public String getToken()
	{
		return token;
	}

	@Override
	public String getName()
	{
		return "CA_SSO_LOGIN_REQ";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_CA_SSO_LOGIN_REQ;
	}

	@Override
	protected int length()
	{
		return 0;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		description.append("version", version);
		description.append("clientType", clientType);
		description.append("username", username);
		description.append("password", password);
		description.append("ip", ip);
		description.append("macAddress", macAddress);
		description.append("token", token);
	}
}

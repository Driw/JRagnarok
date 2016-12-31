package org.diverproject.jragnarok.packets.login.toclient;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AC_ACCEPT_LOGIN;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.login.CharServerList;
import org.diverproject.jragnarok.server.login.ClientCharServer;
import org.diverproject.jragnarok.server.login.LoginSessionData;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.lang.Bits;
import org.diverproject.util.stream.Output;

public class AC_AccepLogin extends ResponsePacket
{
	private CharServerList servers;
	private LoginSessionData sd;

	@Override
	protected void sendOutput(Output output)
	{
		int length = length() + 2;

		output.putShort(s(length));
		output.putInt(sd.getSeed().getFirst());
		output.putInt(sd.getID());
		output.putInt(sd.getSeed().getSecond());

		/* Era usado em versões antigas
		 * output.putInt(sd.getAddress());
		 * output.putString(sd.getLastLogin().toString(), 26);
		 */

		output.skipe(4);
		output.skipe(26);
		output.skipe(1);

		for (ClientCharServer server : servers)
		{
			if (!server.getFileDecriptor().isConnected())
				continue;

			output.putInt(Bits.swap(server.getIP().get())); // TODO IP de LAN ou WAN : loginclif.c:128
			output.putShort(s(server.getPort()));
			output.putString(server.getName(), 20);
			output.putShort(server.getUsers());
			output.putShort(server.getType().CODE);
			output.putShort(s(server.isNewDisplay() ? 1 : 0));
		}
	}

	public void setServers(CharServerList servers)
	{
		this.servers = servers;
	}

	public void setSessionData(LoginSessionData sd)
	{
		this.sd = sd;
	}

	@Override
	public String getName()
	{
		return "AC_ACCEPT_LOGIN";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_AC_ACCEPT_LOGIN;
	}

	@Override
	protected int length()
	{
		return 45 + (32 * servers.size());
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		super.toString(description);

		if (sd != null)
			description.append("aid", sd.getID());

		if (servers != null)
			for (ClientCharServer server : servers)
				description.append(server.getName(), server.getUsers()+ " onlines");
	}
}

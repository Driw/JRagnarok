package org.diverproject.jragnarok.packets.response;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_LIST_SERVERS;

import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.server.login.CharServerList;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;
import org.diverproject.util.stream.Output;

public class ListCharServers extends ResponsePacket
{
	private CharServerList servers;
	private LoginSessionData sd;

	@Override
	protected void sendOutput(Output output)
	{
		int length = 47 + (32 * servers.size());

		output.putShort(s(length));
		output.putInt(sd.getSeed().getFirst());
		output.putInt(sd.getID());
		output.putInt(sd.getSeed().getSecond());

		/* Era usado em versões antigas
		 * output.putInt(sd.getAddress());
		 * output.putInt(sd.getLastLogin().get());
		 */

		output.skipe(4);
		output.skipe(24);
		output.skipe(3);

		for (ClientCharServer server : servers)
		{
			if (!server.getFileDecriptor().isConnected())
				continue;

			// TODO IP de LAN ou WAN : loginclif.c:128

			output.putInt(server.getIP().get());
			output.putShort(s(server.getPort()));
			output.putString(server.getName(), 20);
			output.putShort(server.getUsers());
			output.putShort(server.getType().CODE);
			output.putShort(server.getNewValue());
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
	protected int length()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "LIST_SERVERS";
	}

	@Override
	public short getIdentify()
	{
		return PACKET_LIST_SERVERS;
	}
}

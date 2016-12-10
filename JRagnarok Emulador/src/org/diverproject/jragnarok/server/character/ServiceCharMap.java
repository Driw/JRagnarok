package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.request.AccountInfoResult;
import org.diverproject.jragnarok.server.character.structures.ClientMapServer;

public class ServiceCharMap extends AbstractCharService
{
	public ServiceCharMap(CharServer server)
	{
		super(server);
	}

	/**
	 * Envia os dados de um mesmo pacote para todos os servidores de mapa conectados.
	 * Apenas aqueles servidores que n�o estejam conectados n�o ir�o receber os dados.
	 * @param packet pacote contendo os dados que ser�o enviados a todos os servidores.
	 */

	public final void sendAll(IResponsePacket packet)
	{
		for (ClientMapServer server : getServer().getMapServers())
			if (server.getFileDecriptor().isConnected())
				packet.send(server.getFileDecriptor());
	}

	public boolean receiveAccountInfo(AccountInfoResult packet)
	{
		// TODO mapif_accinfo_ack

		return false;
	}

	public boolean receiveVipData(/*VipDataResult packet*/)
	{
		// TODO Auto-generated method stub

		return false;
	}

	public void disconnectPlayer(CFileDescriptor fd, int accountID, int charID, int reason) // TODO reason enum
	{
		// TODO mapif_disconnectplayer

	}
}

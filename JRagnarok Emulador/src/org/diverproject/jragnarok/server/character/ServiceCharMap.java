package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.packets.IResponsePacket;
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
}

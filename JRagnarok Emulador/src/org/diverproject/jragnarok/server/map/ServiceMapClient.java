package org.diverproject.jragnarok.server.map;

import static org.diverproject.log.LogSystem.logDebug;

import org.diverproject.jragnarok.packets.inter.mapchar.ZH_KeepAlive;
import org.diverproject.jragnarok.server.map.structures.MapSessionData;

/**
 * <h1>Serviço para Comunicação com o Cliente</h1>
 *
 * @author Andrew
 */

public class ServiceMapClient extends AbstractMapService
{
	public ServiceMapClient(MapServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Envia um pacote para uma conexão afim de mantê-la viva no sistema.
	 * Esse pacote é enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void keepAliveCharServer(MFileDescriptor fd)
	{
		if (fd.isConnected())
		{
			MapSessionData sd = fd.getSessionData();

			logDebug("pingar servidor de personagem (server-fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

			ZH_KeepAlive packet = new ZH_KeepAlive();
			packet.send(fd);
		}
	}
}

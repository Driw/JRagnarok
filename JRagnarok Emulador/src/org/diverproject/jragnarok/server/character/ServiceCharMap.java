package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_FAILURE;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_FULL;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_SUCCESSFUL;

import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.common.ResultMapServerConnection;
import org.diverproject.jragnarok.packets.inter.charmap.HZ_ResultMapServerConnection;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AccountInfo;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_MapServerConnection;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.DisconnectPlayer;

public class ServiceCharMap extends AbstractCharService
{
	public ServiceCharMap(CharServer server)
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
	 * Analisa uma conexão de um servidor de mapa que está tentando se conectar com o servidor de personagem.
	 * @param fd conexão do descritor de arquivo do servidor de mapa com o servidor de personagem.
	 * @return true se a conexão for autorizada ou false caso contrário (false também fecha a conexão).
	 */

	public boolean parse(CFileDescriptor fd)
	{
		ZH_MapServerConnection packet = new ZH_MapServerConnection();
		packet.receive(fd);

		if (getServer().getMapServers().isFull())
		{
			notifyConnection(fd, RMSC_FULL);
			return false;
		}

		ClientMapServer server = new ClientMapServer(fd);
		server.setIP(new InternetProtocol(packet.getIpAddress()));
		server.setPort(packet.getPort());
		server.setUsers(s(0));

		if (getServer().getMapServers().add(server))
		{
			notifyConnection(fd, RMSC_SUCCESSFUL);

			fd.getFlag().set(CFileDescriptor.FLAG_SERVER);
			fd.setParseListener(getServer().getFacade().PARSE_MAP_SERVER);

			return true;
		}

		notifyConnection(fd, RMSC_FAILURE);
		return false;
	}

	/**
	 * Notifica ao servidor de mapa o resultado da sua tentativa de conexão com o servidor de personagem.
	 * @param fd conexão do descritor de arquivo do servidor de mapa com o servidor de personagem.
	 * @param result resultado obtido da tentativa de conectar-se com o servidor de personagem.
	 */

	private void notifyConnection(CFileDescriptor fd, ResultMapServerConnection result)
	{
		HZ_ResultMapServerConnection packet = new HZ_ResultMapServerConnection();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia os dados de um mesmo pacote para todos os servidores de mapa conectados.
	 * Apenas aqueles servidores que não estejam conectados não irão receber os dados.
	 * @param packet pacote contendo os dados que serão enviados a todos os servidores.
	 */

	public final void sendAll(IResponsePacket packet)
	{
		for (ClientMapServer server : getServer().getMapServers())
			if (server.getFileDecriptor().isConnected())
				packet.send(server.getFileDecriptor());
	}

	/**
	 * 
	 * @param packet
	 * @return
	 */

	public boolean receiveAccountInfo(AH_AccountInfo packet)
	{
		// TODO mapif_accinfo_ack

		return false;
	}

	/**
	 * 
	 * @return
	 */

	public boolean receiveVipData(/*VipDataResult packet*/)
	{
		// TODO Auto-generated method stub

		return false;
	}

	/**
	 * 
	 * @param fd
	 * @param charID
	 * @param reason
	 */

	public void disconnectPlayer(CFileDescriptor fd, int charID, DisconnectPlayer reason)
	{
		// TODO mapif_disconnectplayer

	}
}

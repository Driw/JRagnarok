package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_DEFAULT_MAP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_DEFAULT_MAP_X;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_DEFAULT_MAP_Y;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_FAILURE;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_FULL;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_SUCCESSFUL;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.database.MapIndexes;
import org.diverproject.jragnaork.database.impl.MapIndex;
import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.common.ResultMapServerConnection;
import org.diverproject.jragnarok.packets.inter.charmap.HZ_ResultMapServerConnection;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AccountInfo;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_MapServerConnection;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_SendMaps;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.DisconnectPlayer;
import org.diverproject.util.collection.Queue;

/**
 * <h1>Servi�o para Comunica��o com o Servidor de Mapa</h1>
 *
 * <p>Atrav�s desse servi�o ser� definido todas as funcionalidades necess�rias para realizar a comunica��o.
 * A comunica��o que se refere � entre o servidor de personagem e o servidor de mapa, aqui dever� ser feita
 * a autentica��o de uma conex�o desta feita como tamb�m manter a mesma funcionando corretamente.</p>
 *
 * @see AbstractCharService
 * @see MapIndexes
 *
 * @author Andrew
 */

public class ServiceCharMap extends AbstractCharService
{
	/**
	 * Indexa��o dos mapas dispon�veis no servidor.
	 */
	private MapIndexes maps;

	/**
	 * Cria uma nova inst�ncia de um servi�o para comunica��o com o servidor de mapas.
	 * @param server refer�ncia do servidor de personagem que ir� utilizar o servi�o.
	 */

	public ServiceCharMap(CharServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		maps = getServer().getFacade().getMapIndexes();		
	}

	@Override
	public void destroy()
	{
		maps = null;
	}

	/**
	 * Analisa uma conex�o de um servidor de mapa que est� tentando se conectar com o servidor de personagem.
	 * @param fd conex�o do descritor de arquivo do servidor de mapa com o servidor de personagem.
	 * @return true se a conex�o for autorizada ou false caso contr�rio (false tamb�m fecha a conex�o).
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
	 * Notifica ao servidor de mapa o resultado da sua tentativa de conex�o com o servidor de personagem.
	 * @param fd conex�o do descritor de arquivo do servidor de mapa com o servidor de personagem.
	 * @param result resultado obtido da tentativa de conectar-se com o servidor de personagem.
	 */

	private void notifyConnection(CFileDescriptor fd, ResultMapServerConnection result)
	{
		HZ_ResultMapServerConnection packet = new HZ_ResultMapServerConnection();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Recebe um pacote que ir� conter as informa��es de todos as indexa��es dos mapas dispon�veis.
	 * Essa indexa��o dos mapas � recebido de um servidor de mapas que efetuou o carregamento dos mesmos.
	 * @param fd conex�o do descritor de arquivo do servidor de mapa com o servidor de personagem.
	 */

	public void receiveMapIndexes(CFileDescriptor fd)
	{
		ZH_SendMaps packet = new ZH_SendMaps();
		packet.receive(fd);

		Queue<MapIndex> queue = packet.getMaps();

		while (!queue.isEmpty())
		{
			MapIndex map = queue.poll();

			if (!maps.insert(map))
				logWarning("n�o foi poss�vel indexar o mapa '%s' (fd: %d, mid: %d).\n", map.getMapName(), fd.getID(), map.getID());
		}

		logInfo("recebido '%d' �ndices de mapas do servidor de mapa (fd: %d).\n", maps.size(), fd.getID());

		String mapname = getConfigs().getString(CHAR_DEFAULT_MAP);

		if (!maps.contains(mapname))
			logWarning("mapa padr�o n�o encontrado na indexa��o de mapas (map: %s).\n", mapname);

		else
		{
			int x = getConfigs().getInt(CHAR_DEFAULT_MAP_X);
			int y = getConfigs().getInt(CHAR_DEFAULT_MAP_Y);

			logInfo("mapa padr�o definido como '%s %d,%d'.\n", mapname, x, y);
		}
	}

	/**
	 * Envia os dados de um mesmo pacote para todos os servidores de mapa conectados.
	 * Apenas aqueles servidores que n�o estejam conectados n�o ir�o receber os dados.
	 * @param packet pacote contendo os dados que ser�o enviados a todos os servidores.
	 */

	public void sendAll(IResponsePacket packet)
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

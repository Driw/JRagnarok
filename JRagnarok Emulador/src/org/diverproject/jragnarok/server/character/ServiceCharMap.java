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
 * <h1>Serviço para Comunicação com o Servidor de Mapa</h1>
 *
 * <p>Através desse serviço será definido todas as funcionalidades necessárias para realizar a comunicação.
 * A comunicação que se refere é entre o servidor de personagem e o servidor de mapa, aqui deverá ser feita
 * a autenticação de uma conexão desta feita como também manter a mesma funcionando corretamente.</p>
 *
 * @see AbstractCharService
 * @see MapIndexes
 *
 * @author Andrew
 */

public class ServiceCharMap extends AbstractCharService
{
	/**
	 * Indexação dos mapas disponíveis no servidor.
	 */
	private MapIndexes maps;

	/**
	 * Cria uma nova instância de um serviço para comunicação com o servidor de mapas.
	 * @param server referência do servidor de personagem que irá utilizar o serviço.
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
	 * Recebe um pacote que irá conter as informações de todos as indexações dos mapas disponíveis.
	 * Essa indexação dos mapas é recebido de um servidor de mapas que efetuou o carregamento dos mesmos.
	 * @param fd conexão do descritor de arquivo do servidor de mapa com o servidor de personagem.
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
				logWarning("não foi possível indexar o mapa '%s' (fd: %d, mid: %d).\n", map.getMapName(), fd.getID(), map.getID());
		}

		logInfo("recebido '%d' índices de mapas do servidor de mapa (fd: %d).\n", maps.size(), fd.getID());

		String mapname = getConfigs().getString(CHAR_DEFAULT_MAP);

		if (!maps.contains(mapname))
			logWarning("mapa padrão não encontrado na indexação de mapas (map: %s).\n", mapname);

		else
		{
			int x = getConfigs().getInt(CHAR_DEFAULT_MAP_X);
			int y = getConfigs().getInt(CHAR_DEFAULT_MAP_Y);

			logInfo("mapa padrão definido como '%s %d,%d'.\n", mapname, x, y);
		}
	}

	/**
	 * Envia os dados de um mesmo pacote para todos os servidores de mapa conectados.
	 * Apenas aqueles servidores que não estejam conectados não irão receber os dados.
	 * @param packet pacote contendo os dados que serão enviados a todos os servidores.
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

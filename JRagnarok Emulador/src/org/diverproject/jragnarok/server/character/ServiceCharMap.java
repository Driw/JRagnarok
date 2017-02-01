package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_SERVERS;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.Util.seconds;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_FAILURE;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_FULL;
import static org.diverproject.jragnarok.packets.common.ResultMapServerConnection.RMSC_SUCCESSFUL;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.format;

import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.database.MapIndexes;
import org.diverproject.jragnaork.database.impl.MapIndex;
import org.diverproject.jragnaork.database.io.IOMapIndex;
import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.common.ResultMapServerConnection;
import org.diverproject.jragnarok.packets.inter.charmap.HZ_KeepAlive;
import org.diverproject.jragnarok.packets.inter.charmap.HZ_ResultMapServerConnection;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AccountInfo;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_MapServerConnection;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_NotifyUserCount;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_SendInformations;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_SendMaps;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerAdapt;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.common.DisconnectPlayer;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.sql.MySQL;

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
	 * Quantas vezes o temporizador poderá esperar a resposta de ping do servidor de acesso.
	 */
	public static final int PING_MAX_WAITING = 3;


	/**
	 * Conexão com o banco de dados MySQL para a base de dados do jogo.
	 */
	private MySQL dbMysql;

	/**
	 * Indexação dos mapas disponíveis no servidor.
	 */
	private MapIndexes maps;

	/**
	 * Vetor para realizar a contagem de ping por servidor.
	 */
	private int pingCount[] = new int[MAX_SERVERS];

	/**
	 * Cria uma nova instância de um serviço para comunicação com o servidor de mapas.
	 * @param server referência do servidor de personagem que irá utilizar o serviço.
	 */

	public ServiceCharMap(CharServer server)
	{
		super(server);
	}

	/**
	 * Verifica se uma conexão de um servidor de mapa com o servidor de personagem está conectada.
	 * @param server referência dos dados do cliente (servidor de mapa) no servidor de personagem.
	 * @return true se a conexão ainda estiver ativa ou false caso contrário.
	 */

	public boolean isConnected(ClientMapServer server)
	{
		return	server != null &&
				server.getFileDescriptor() != null &&
				server.getFileDescriptor().isConnected() &&
				!server.getFileDescriptor().getFlag().is(FileDescriptor.FLAG_EOF);
	}

	@Override
	public void init()
	{
		maps = getServer().getFacade().getMapIndexes();		

		initDatabaseMySQL();
		readMapIndex();

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer keepAlive = timers.acquireTimer();
		keepAlive.setListener(KEEP_ALIVE);
		keepAlive.setTick(ts.getCurrentTime() + seconds(10));
		timers.addLoop(keepAlive, seconds(10));
	}

	@Override
	public void destroy()
	{
		maps = null;
	}

	/**
	 * Cria uma conexão com o banco de dados MySQL utilizando o banco de dados que contém a base de dados do jogo.
	 * A base de dados do jogo consiste em informações que são carregadas antes do servidor carregar por completo.
	 * Essas informações podem ser de mapas, itens, classes, mapas, scripts e outros tipos de dados.
	 */

	private void initDatabaseMySQL()
	{
		String host = config().databaseHost;
		String database = config().databaseName;
		String username = config().databaseUsername;
		String password = config().databasePassword;
		short port = config().databasePort;

		try {

			dbMysql = new MySQL();
			dbMysql.setHost(host);
			dbMysql.setPort(port);
			dbMysql.setDatabase(database);
			dbMysql.setUsername(username);
			dbMysql.setPassword(password);
			dbMysql.connect();

			logNotice("banco de dados para base de dados do jogo conectado (%s@%s:%d).\n", host, database, port);

		} catch (SQLException | ClassNotFoundException e) {
			logError("falha ao conectar-se com o banco de dados da base de dados do jogo:\n");
			logException(e);
		}
	}

	/**
	 * Solicita a leitura dos dados para indexação dos mapas disponíveis e que poderão ser carregados no jogo.
	 * Cada mapa será vinculado a um código de identificação para que possa ser localizado e identificado.
	 * A leitura pode ser feita através de um arquivo de texto formatado ou um banco de dados MySQL.
	 */

	private void readMapIndex()
	{
		IOMapIndex io = new IOMapIndex();
		io.getPreferences().set(IOMapIndex.DEFAULT_PREFERENCES);
		io.getPreferences().set(IOMapIndex.PREFERENCES_INTERNAL_LOG_READ);

		String folder = config().databaseFolder;
		String filename = config().mapIndexes;

		try {

			if (filename.endsWith(".sql"))
				io.readSQL(maps, dbMysql.getConnection(), folder);

			else if (filename.endsWith(".txt"))
				io.readFile(maps, format("%s/%s", folder, filename));

			else
				throw new RagnarokException("'map_index' com formato inválido (value: %s)", filename);

		} catch (RagnarokException e) {
			logError("falha durante a leitura de '%s':\n", filename);
			logException(e);
		}

		while (!io.getExceptions().isEmpty())
			logException(io.getExceptions().poll());
	}

	/**
	 * Listener usado para manter a conexão entre o servidor de acesso e o servidor de personagem.
	 */

	private final TimerListener KEEP_ALIVE = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			for (ClientMapServer server : getServer().getMapServers())
				if (isConnected(server))
					ping(server);
		}

		/**
		 * Procedimento interno para realizar o processo de envio do ping a um servidor de mapa.
		 * @param server referência dos dados do cliente (servidor de mapa) no servidor de personagem.
		 */

		private void ping(ClientMapServer server)
		{
			if (server.getFileDescriptor().getFlag().is(FileDescriptor.FLAG_PING))
			{
				if (pingCount[server.getID()] == PING_MAX_WAITING)
				{
					logNotice("conexão com o servidor de acesso fechado por falta de resposta.\n");
					server.getFileDescriptor().close();
					return;
				}

				else
					logWarning("aguardando servidor de acesso (%d de %d tentativas)...\n", pingCount[server.getID()]++, PING_MAX_WAITING);
			}

			HZ_KeepAlive packet = new HZ_KeepAlive();
			packet.send(server.getFileDescriptor());

			server.getFileDescriptor().getFlag().set(FileDescriptor.FLAG_PING);
		}

		@Override
		public String getName()
		{
			return "KEEP_ALIVE";
		}
	};

	/**
	 * Mantém a conexão de um descritor de arquivo vida dentro do sistema com um "ping".
	 * @param lfd conexão do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se ainda estiver conectado ou false caso contrário.
	 */

	public void keepAlive(CFileDescriptor fd)
	{
		fd.getFlag().unset(FileDescriptor.FLAG_PING);
		ClientMapServer server = getServer().getMapServers().get(fd);

		if (server != null)
			pingCount[server.getID()] = 0;
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
		MapIndexes maps = new MapIndexes();

		while (!queue.isEmpty())
		{
			MapIndex map = queue.poll();

			if (!maps.insert(map))
				logWarning("não foi possível indexar o mapa '%s' (fd: %d, mid: %d).\n", map.getMapName(), fd.getID(), map.getID());
		}

		logInfo("recebido '%d' índices de mapas do servidor de mapa (fd: %d).\n", maps.size(), fd.getID());

		String mapname = config().defaultMap;

		if (!maps.contains(mapname))
			logWarning("mapa padrão não encontrado na indexação de mapas (map: %s).\n", mapname);

		else
		{
			int x = config().defaultMapX;
			int y = config().defaultMapY;

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
			if (server.getFileDescriptor().isConnected())
				packet.send(server.getFileDescriptor());
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

	/**
	 * Verifica todos os servidores de mapas conectados afim de encontrar um mapa especificado.
	 * @param mapID código de identificação do mapa do qual deseja localizar em um servidor.
	 * @param ip endereço de IP do servidor a considerar ou -1 (menos um) para qualquer um.
	 * @param port porta de conexão do servidor a considerar ou -1 (menos um) para qualquer um.
	 * @return aquisição do código de identificação do servidor de mapas que contém o mapa acima.
	 */

	public int searchMapServerID(short mapID, int ip, short port)
	{
		for (ClientMapServer server : getServer().getMapServers())
			if (server.getFileDescriptor().isConnected() &&
				(server.getIP().get() == ip || ip == -1) &&
				(server.getPort() == port || port == -1))
			{
				for (int i = 0; i < server.getMaps().length; i++)
					if (server.getMaps()[i] != null && server.getMaps()[i] == mapID)
						return i;
			}

		return OnlineCharData.NO_SERVER;
	}

	/**
	 * Verifica se um determinado servidor de mapa possui conexão com este servidor de personagem.
	 * @param mapServerID código de identificação do servidor de mapa do qual será verificado.
	 * @return true se houver um conexão estabelecida ou false caso contrário.
	 */

	public boolean hasConnection(int mapServerID)
	{
		ClientMapServer server = getServer().getMapServers().get(mapServerID);

		if (server != null)
		{
			if (server.getFileDescriptor() != null && server.getFileDescriptor().isConnected())
				return false;

			getServer().getMapServers().remove(server);
		}

		return false;
	}

	/**
	 * verifica se há ao menos um servidor de mapas conectado no servidor de personagem.
	 * @return true se houver ao menos um conectado ou false se não houver nenhum.
	 */

	public boolean hasConnection()
	{
		for (ClientMapServer server : getServer().getMapServers())
			if (server.getFileDescriptor().isConnected())
				return true;

		return false;
	}

	/**
	 * Recebe uma notificação de um servidor de mapas a quantidade de jogadores online no mesmo.
	 * Após receber a quantidade deverá atualizar o número de jogadores online no cliente respectivo.
	 * @param fd conexão do descritor de arquivo do servidor de mapa com o servidor de personagem.
	 */

	public void receiveUserCount(CFileDescriptor fd)
	{
		ZH_NotifyUserCount packet = new ZH_NotifyUserCount();
		packet.receive(fd);

		ClientMapServer server = getServer().getMapServers().get(fd);

		if (server != null)
			server.setUsers(packet.getUserCount());
	}

	/**
	 * Recebe as informações básicas de um servidor de mapa que foi conectado ao servidor de personagem.
	 * @param fd conexão do descritor de arquivo do servidor de mapa com o servidor de personagem.
	 */

	public void receiveInformations(CFileDescriptor fd)
	{
		ZH_SendInformations packet = new ZH_SendInformations();
		packet.receive(fd);

		// TODO
	}
}

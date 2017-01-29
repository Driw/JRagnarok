package org.diverproject.jragnarok.server.map;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_MAP_INDEX;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_DATABASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_HOST;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.DATABASE_SQL_USERNAME;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.util.Util.format;
import static org.diverproject.util.Util.seconds;

import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.database.MapIndexes;
import org.diverproject.jragnaork.database.io.IOMapIndex;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerAdapt;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.util.sql.MySQL;

/**
 * 
 * @author Andrew
 */

public class ServiceMapServer extends AbstractMapService
{
	/**
	 * Conexão com o banco de dados MySQL para a base de dados do jogo.
	 */
	private MySQL dbMysql;

	/**
	 * Indexação de todos os mapas do jogo.
	 */
	private MapIndexes indexes;

	/**
	 * Cria uma nova instância de um serviço para gerenciamento do servidor de mapa.
	 * @param server referência do servidor de mapa que irá utilizar o serviço.
	 */

	public ServiceMapServer(MapServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		indexes = getServer().getFacade().getMapIndexes();

		initDatabaseMySQL();
		initMapData();
		initGameFunctions();
	}

	/**
	 * Procedimento chamado durante a inicialização do serviço de gerenciamento do servidor de mapas.
	 * Esse método deverá realizar a leitura de todas as informações básicas dos mapas disponíveis.
	 * A primeira leitura consiste em identificar os mapas disponíveis e as outras em seus dados.
	 * Os dados por sua vez determinando as partes do mapa que podem caminhadas ou não (exemplo).
	 */

	private void initMapData()
	{
		// TODO map.c:map_reloadnpc [4654] reloadNPC();
		readMapIndex();
		// TODO map.c:grfio_init [4700] readAllMaps();
		// TODO map.c:map_readallmaps [4702] readMapCache();

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer timer = timers.acquireTimer();
		timer.setTick(ts.getCurrentTime() + seconds(1));
		timer.setListener(MAP_FREEBLOCK);
		timers.addLoop(timer, seconds(60));
	}

	/**
	 * Procedimento chamado durante a inicialização do serviço de gerenciamento do servidor de mapas.
	 * Deverá garantir a inicialização de alguns dados/temporizadores que possam ser necessários no jogo.
	 * Todos esses dados ou funcionalidades são utilizados em quanto um personagem está em mapa.
	 */

	private void initGameFunctions()
	{
		// TODO map.c:map_readallmaps [4710] initPath();
		// TODO map.c:map_readallmaps [4711] initAtcommand();
		// TODO map.c:map_readallmaps [4712] initBattle();
		// TODO map.c:map_readallmaps [4713] initInstance();
		// TODO map.c:map_readallmaps [4714] initChannel();
		// TODO map.c:map_readallmaps [4715] initChrif();
		// TODO map.c:map_readallmaps [4716] initClif();
		// TODO map.c:map_readallmaps [4717] initScript();
		// TODO map.c:map_readallmaps [4718] initItemdb();
		// TODO map.c:map_readallmaps [4719] initCashshop();
		// TODO map.c:map_readallmaps [4720] initSkill();
		// TODO map.c:map_readallmaps [4721] initMob();
		// TODO map.c:map_readallmaps [4722] initPC();
		// TODO map.c:map_readallmaps [4723] initStatus();
		// TODO map.c:map_readallmaps [4724] initParty();
		// TODO map.c:map_readallmaps [4725] initGuild();
		// TODO map.c:map_readallmaps [4726] initStorage();
		// TODO map.c:map_readallmaps [4727] initPet();
		// TODO map.c:map_readallmaps [4728] initHomunculus();
		// TODO map.c:map_readallmaps [4729] initMercenary();
		// TODO map.c:map_readallmaps [4730] initElemental();
		// TODO map.c:map_readallmaps [4731] initQuest();
		// TODO map.c:map_readallmaps [4732] initNPC();
		// TODO map.c:map_readallmaps [4733] initUnit();
		// TODO map.c:map_readallmaps [4734] initBattleground();
		// TODO map.c:map_readallmaps [4735] initDuel();
		// TODO map.c:map_readallmaps [4736] initVending();
		// TODO map.c:map_readallmaps [4737] initBuyingstore();		
	}

	@Override
	public void destroy()
	{
		indexes = null;

		try {
			dbMysql.closeConnection();
		} catch (SQLException e) {
			logError("falha ao fechar conexão com base de dados do jogo:\n");
			logException(e);
		}
	}

	private final TimerListener MAP_FREEBLOCK = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			// TODO map.c:map_freeblock_timer [4707]
		}
		
		@Override
		public String getName()
		{
			return "MAP_FREEBLOCK";
		}
	};

	/**
	 * Cria uma conexão com o banco de dados MySQL utilizando o banco de dados que contém a base de dados do jogo.
	 * A base de dados do jogo consiste em informações que são carregadas antes do servidor carregar por completo.
	 * Essas informações podem ser de mapas, itens, classes, mapas, scripts e outros tipos de dados.
	 */

	private void initDatabaseMySQL()
	{
		String host = getConfigs().getString(DATABASE_SQL_HOST);
		String database = getConfigs().getString(DATABASE_SQL_DATABASE);
		String username = getConfigs().getString(DATABASE_SQL_USERNAME);
		String password = getConfigs().getString(DATABASE_SQL_PASSWORD);
		int port = getConfigs().getInt(DATABASE_SQL_PORT);

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

		String folder = getConfigs().getString(DATABASE_FOLDER);
		String filename = getConfigs().getString(DATABASE_MAP_INDEX);

		try {

			if (filename.endsWith(".sql"))
				io.readSQL(indexes, dbMysql.getConnection(), folder);

			else if (filename.endsWith(".txt"))
				io.readFile(indexes, format("%s/%s", folder, filename));

			else
				throw new RagnarokException("'%s' com formato inválido (value: %s)", DATABASE_MAP_INDEX, filename);

		} catch (RagnarokException e) {
			logError("falha durante a leitura de '%s':\n", filename);
			logException(e);
		}

		while (!io.getExceptions().isEmpty())
			logException(io.getExceptions().poll());
	}
}

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
	 * Conex�o com o banco de dados MySQL para a base de dados do jogo.
	 */
	private MySQL dbMysql;

	/**
	 * Indexa��o de todos os mapas do jogo.
	 */
	private MapIndexes indexes;

	/**
	 * Cria uma nova inst�ncia de um servi�o para gerenciamento do servidor de mapa.
	 * @param server refer�ncia do servidor de mapa que ir� utilizar o servi�o.
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
	 * Procedimento chamado durante a inicializa��o do servi�o de gerenciamento do servidor de mapas.
	 * Esse m�todo dever� realizar a leitura de todas as informa��es b�sicas dos mapas dispon�veis.
	 * A primeira leitura consiste em identificar os mapas dispon�veis e as outras em seus dados.
	 * Os dados por sua vez determinando as partes do mapa que podem caminhadas ou n�o (exemplo).
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
	 * Procedimento chamado durante a inicializa��o do servi�o de gerenciamento do servidor de mapas.
	 * Dever� garantir a inicializa��o de alguns dados/temporizadores que possam ser necess�rios no jogo.
	 * Todos esses dados ou funcionalidades s�o utilizados em quanto um personagem est� em mapa.
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
			logError("falha ao fechar conex�o com base de dados do jogo:\n");
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
	 * Cria uma conex�o com o banco de dados MySQL utilizando o banco de dados que cont�m a base de dados do jogo.
	 * A base de dados do jogo consiste em informa��es que s�o carregadas antes do servidor carregar por completo.
	 * Essas informa��es podem ser de mapas, itens, classes, mapas, scripts e outros tipos de dados.
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
	 * Solicita a leitura dos dados para indexa��o dos mapas dispon�veis e que poder�o ser carregados no jogo.
	 * Cada mapa ser� vinculado a um c�digo de identifica��o para que possa ser localizado e identificado.
	 * A leitura pode ser feita atrav�s de um arquivo de texto formatado ou um banco de dados MySQL.
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
				throw new RagnarokException("'%s' com formato inv�lido (value: %s)", DATABASE_MAP_INDEX, filename);

		} catch (RagnarokException e) {
			logError("falha durante a leitura de '%s':\n", filename);
			logException(e);
		}

		while (!io.getExceptions().isEmpty())
			logException(io.getExceptions().poll());
	}
}

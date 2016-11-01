package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_HOST;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_THREAD_PRIORITY;
import static org.diverproject.jragnarok.JRagnarokUtil.format;
import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.JRagnarokUtil.sleep;
import static org.diverproject.jragnarok.JRagnarokUtil.time;
import static org.diverproject.jragnarok.server.ServerState.CREATED;
import static org.diverproject.jragnarok.server.ServerState.DESTROYED;
import static org.diverproject.jragnarok.server.ServerState.NONE;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.ServerState.STOPED;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.configuration.ConfigReader;
import org.diverproject.jragnaork.configuration.Configurations;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.lang.IntUtil;
import org.diverproject.util.lang.ShortUtil;
import org.diverproject.util.sql.MySQL;

/**
 * <h1>Servidor</h1>
 *
 * <p>Classe usada para definir um sistema de servidor com as informa��es e m�todos para tal.
 * As informa��es referentes � de um ServerSocket para receber a conex�o socket dos clientes.
 * Possui ainda um Thread para processar esses clientes recebidos e conex�o com o banco de dados.</p>
 *
 * <p>Al�m do b�sico para um servidor, possui um listener de servidor implementado nas herdeiras.
 * Tamb�m possui um ServerConfig que ir� armazenar todas as configura��es pertinentes ao mesmo.
 * Por �ltimo determina um FileDescriptorListener que determina quem ir� analisar os clientes.</p>
 *
 * @author Andrew
 */

public abstract class Server
{
	/**
	 * Porta m�nima aceita pelos servidores.
	 */
	private static final int MIN_PORT = 1001;

	/**
	 * Porta m�xima aceita pelos servidores.
	 */
	private static final int MAX_PORT = 65535;

	/**
	 * TODO what is that?
	 */
	private static final int SOCKET_BACKLOG = 50;

	/**
	 * Valor das prefer�ncias para definir o leitor de configura��es:
	 * <code>INTERNAL_LOG_ALL</code>, <code>LOG_EXCEPTIONS</code>, <code>THROWS_FORMAT</code>,
	 * <code>THROWS_EXCEPTIONS</code>, <code>THROWS_NOTFOUND</code> e <code>THROWS_UNEXPECETED</code>.
	 */
	public static final int CONFIG_READ_PREFERENCES =
			ConfigReader.PREFERENCES_INTERNAL_LOG_READ +
			ConfigReader.PREFERENCES_LOG_EXCEPTIONS +
			ConfigReader.PREFERENCES_THROWS_FORMAT +
			ConfigReader.PREFERENCES_THROWS_EXCEPTIONS +
			ConfigReader.PREFERENCES_THROWS_NOTFOUND +
			ConfigReader.PREFERENCES_THROWS_UNEXPECTED;


	/**
	 * C�digo de identifica��o do servidor de acesso.
	 */
	private int id;

	/**
	 * Thread que ser� usada para receber as conex�es sockets.
	 */
	private Thread threadSocket;

	/**
	 * Thread que ser� usada para manter o servidor rodando.
	 */
	private Thread threadServer;

	/**
	 * Estado do servidor para garantir ordem nas chamadas.
	 */
	private ServerState state;

	/**
	 * Conex�o socket de servidor para receber os clientes.
	 */
	private ServerSocket serverSocket;

	/**
	 * Listener usado pelas classes herdeiras afim de executar opera��es especificas.
	 */
	private ServerListener listener;

	/**
	 * Configura��es do servidor.
	 */
	private Configurations configs;

	/**
	 * Listener para despachar os Arquivos Descritores.
	 */
	private FileDescriptorListener defaultParser;

	/**
	 * Conex�o com o banco de dados MySQL.
	 */
	private MySQL sql;

	/**
	 * Sitema de temporiza��o do servidor.
	 */
	private TimerSystem timerSystem;

	/**
	 * Sistema para cria��o de Descritor de Arquivos.
	 */
	private FileDescriptorSystem fileDescriptorSystem;

	/**
	 * Cria um novo servidor definindo o servidor no estado NONE (nenhum/inicial).
	 * Tamb�m define as configura��es do servidor por setServerConfig().
	 * E por fim instancia o objeto para criar a conex�o com o banco de dados.
	 */

	public Server()
	{
		state = NONE;
		sql = new MySQL();
		timerSystem = new TimerSystem();
		fileDescriptorSystem = new FileDescriptorSystem(timerSystem);
	}

	/**
	 * O c�digo de identifica��o � individual para cada tipo de servidor.
	 * @return aquisi��o do c�digo de identifica��o do servidor.
	 */

	public final int getID()
	{
		return id;
	}

	/**
	 * O c�digo de identifica��o � individual para cada tipo de servidor.
	 * Acesso por pacote e utilizado por ServerControl quando listado.
	 * @param id c�digo de identifica��o do servidor.
	 * @see ServerController
	 */

	public final void setID(int id)
	{
		if (this.id == 0)
			this.id = id;
	}

	/**
	 * @return aquisi��o do nome que ser� dado a thread do servidor.
	 */

	protected final String getThreadName()
	{
		return format("%s#%d@%s:%d", nameOf(this), id, getHost(), getPort());
	}

	/**
	 * @return aquisi��o do n�vel de prioridade da thread (MIN_PRIORITY ou MAX_PRIORITY).
	 */

	public int getThreadPriority()
	{
		int value = configs.getInt(SERVER_THREAD_PRIORITY);

		return IntUtil.limit(value, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
	}

	/**
	 * Nome do host permite definir a interface para recebimento das conex�es.
	 * @return aquisi��o do host para realizar a conex�o socket (ip ou dom�nio).
	 */

	public String getHost()
	{
		String value = configs.getString(SERVER_HOST);

		return value == null || value.isEmpty() ? "localhost" : value;
	}

	/**
	 * Atrav�s da porta � poss�vel saber por onde as conex�es s�o recebidas na m�quina.
	 * @return aquisi��o da porta em que o servidor ir� receber as conex�es.
	 */

	public short getPort()
	{
		short port = s(configs.getInt(SERVER_PORT));

		return ShortUtil.min(port, s(1001));
	}

	/**
	 * O estado do servidor pode ser �til para realizar determinadas opera��es.
	 * As vezes � necess�rio que o servidor esteja ou n�o rodando para tal.
	 * @return aquisi��o do estado do servidor no momento.
	 */

	public ServerState getState()
	{
		return state;
	}

	/**
	 * Verifica se o servidor se encontra em um determinado estado especificado.
	 * @param state refer�ncia da enumera��o do estado do qual deseja verificar.
	 * @return true se estiver no estado passado por par�metro ou false caso contr�rio.
	 */

	public boolean isState(ServerState state)
	{
		return this.state.equals(state);
	}

	/**
	 * Permite definir qual ser� o listener usado para executar as opera��es nas trocas de estado.
	 * @param listener refer�ncia do objeto que implementou a interface desse listener.
	 */

	protected void setListener(ServerListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Permite obter a refer�ncia do objeto com as configura��es do servidor.
	 * @return aquisi��o do objeto com as configura��es do servidor.
	 */

	public Configurations getConfigs()
	{
		return configs;
	}

	/**
	 * Permite definir qual ser� o objeto usado para armazenar as configura��es do servidor.
	 * Pode ser definido apenas uma �nica vez, logo se j� tiver sido definido n�o ter� efeito.
	 * @param configs refer�ncia do objeto contendo as configura��es do servidor.
	 */

	public void setConfigurations(Configurations configs)
	{
		if (this.configs == null && configs != null)
			this.configs = configs;
	}

	/**
	 * O analisador padr�o � usado para determinar o despache dos novos clientes.
	 * Toda nova conex�o recebida ser� despachada para esse listener.
	 * @param defaultParser refer�ncia do objeto que implementa esse listener.
	 */

	protected void setDefaultParser(FileDescriptorListener defaultParser)
	{
		this.defaultParser = defaultParser;
	}

	/**
	 * Uma conex�o com o banco de dados MySQL � essencial para funcionamento do servidor.
	 * A conex�o ser� realizada durante a cria��o do servidor e fechada na destrui��o.
	 * Utiliza as configura��es do servidor para definir senha, usu�rio e banco de dados.
	 * @return aquisi��o do objeto que permite realizar a conex�o com o banco de dados.
	 */

	public MySQL getMySQL()
	{
		return sql;
	}

	/**
	 * Os servidores possui um sistema de temporiza��o individual para calcular os ticks.
	 * Cada tick represente um milissegundo no tempo real e influencia em chamados.
	 * @return aquisi��o do sistema para controle da temporiza��o do servidor.
	 */

	public TimerSystem getTimerSystem()
	{
		return timerSystem;
	}

	/**
	 * Todo servidor precisa de um sistema que controle as sess�es de conex�es feitas.
	 * @return aquisi��o do sistema que efetua o controle das sess�es no servidor.
	 */

	public FileDescriptorSystem getFileDescriptorSystem()
	{
		return fileDescriptorSystem;
	}

	/**
	 * Procedimento interno que permite alterar o estado em que o servidor se encontra.
	 * Para que um estado seja alterado pode ser necess�rio encontrar-se em outro.
	 * Por exemplo, para entrar no estado de CREATED precisa estar em CREATE.
	 */

	protected void setNextState()
	{
		ServerState old = state;

		switch (state)
		{
			case NONE:
				state = CREATED;
				break;

			case CREATED:
				state = RUNNING;
				break;

			case RUNNING:
				state = STOPED;
				break;

			case STOPED:
				state = DESTROYED;
				break;

			case DESTROYED:
				state = NONE;
				break;
		}

		logInfo("alterando estado de %s para %s.\n", old, state);
	}

	/**
	 * Cria inst�ncias necess�rias de objetos dos quais ser�o utilizados.
	 * Essas inst�ncias consistem em iniciar os valores padr�es das configura��es,
	 * Inicia o m�todo em estado CREATE e passa ao final do m�todo para CREATED.
	 * conectar-se com o banco de dados SQL, temporizadores, thread e socket.
	 * @throws RagnarokException quando houver problemas em uma inicializa��o.
	 */

	public final void create() throws RagnarokException
	{
		if (!isState(NONE) && !isState(DESTROYED))
			throw new RagnarokException("o servidor n�o pode dar create em %s", state);

		listener.onCreate();
		{
			initConfigs();
			initSqlConnection();
			initTimer();
			initThreads();
			initSocket();
		}
		listener.onCreated();
		setNextState();
	}

	/**
	 * Ao ser chamado ir� rodar o servidor, para isso ir� trabalhar com a thread.
	 * A thread � iniciada ou retorna a rodar se estiver em STOPPED.
	 * Ap�s rodar a thread e executar o listener ir� entrar em RUNNING.
	 * @throws RagnarokException estado incorreto ou thread n�o criada.
	 */

	@SuppressWarnings("deprecation")
	public final void run() throws RagnarokException
	{
		if (!isState(CREATED))
			throw new RagnarokException("o servidor n�o pode dar run em %s", state);

		if (threadSocket == null || threadServer == null)
			throw new RagnarokException("thread n�o criada");

		listener.onRunning();
		{
			if (threadSocket.isInterrupted())
				threadSocket.resume();
			else
				threadSocket.start();

			if (threadServer.isInterrupted())
				threadServer.resume();
			else
				threadServer.start();
		}

		setNextState();
	}

	/**
	 * Ao ser chamado ir� parar o servidor, para isso interrompe a thread.
	 * Conex�o com o banco de dados e server socket continuam ativas.
	 * Por�m como a thread est� inativa os clientes ficaram sem sinal.
	 * @throws RagnarokException erro no listener ou thread nula.
	 */

	public final void stop() throws RagnarokException
	{
		if (!isState(RUNNING))
			throw new RagnarokException("o servidor n�o pode dar stop em %s", state);

		if (threadSocket == null || threadServer == null)
			throw new RagnarokException("thread n�o encontrada");

		listener.onStop();
		{
			threadSocket.interrupt();
			threadServer.interrupt();
		}
		setNextState();
	}

	/**
	 * Realiza a destrui��o do servidor, deve chamar a destrui��o dos outros objetos.
	 * A destrui��o consiste no listener que pode solicitar a limpeza de uma cole��o.
	 * Fecha a conex�o socket parando de receber novos clientes e interrompe a thread.
	 * Inicia o processo entrando no estado de DESTROY e termina como DESTROYED.
	 * @throws RagnarokException estado incorreto ou falha ao fechar conex�o.
	 */

	public final void destroy() throws RagnarokException
	{
		if (!isState(STOPED))
			throw new RagnarokException("o servidor n�o pode dar destroy em %s", state);

		try {

			listener.onDestroy();
			{
				serverSocket.close();

				setNextState();

				threadSocket.interrupt();
				threadServer.interrupt();
				threadSocket = null;
				threadServer = null;
			}
			listener.onDestroyed();

		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Inicializa��o das configura��es ir� carregar todas as configura��es necess�rias.
	 * Para o servidor � considerado apenas configura��es do banco de dados.
	 * @throws RagnarokException falha durante o carregamento das configura��es.
	 */

	protected void initConfigs() throws RagnarokException
	{
		String filenames[] = configs.getString(SERVER_FILES).split(",");

		for (String filename : filenames)
		{
			String folder = configs.getString(SERVER_FOLDER);
			String filePath = format("config/Servers/%s/%s", folder, filename.trim());

			readConfigFile(filePath);
		}

		filenames = configs.getString(SYSTEM_SERVER_DEFAULT_FILES).split(",");

		for (String filename : filenames)
		{
			String folder = configs.getString(SYSTEM_SERVER_DEFAULT_FOLDER);
			String filePath = format("config/Servers/%s/%s", folder, filename.trim());

			readConfigFile(filePath);
		}
	}

	/**
	 * Efetua a leitura de um arquivo de configura��es atualizando as configura��es do servidor.
	 * @param filePath caminho completo ou parcial do arquivo de configura��es.
	 */

	private void readConfigFile(String filePath)
	{
		ConfigReader load = new ConfigReader();
		load.getPreferences().set(CONFIG_READ_PREFERENCES);
		load.setConfigurations(configs);
		load.setFilePath(filePath);

		try {
			load.read();
		} catch (RagnarokException e) {
			logError("falha ao ler '%s'.\n", filePath);
			logExeception(e);
		}
	}

	/**
	 * Inicializa��o da conex�o com o banco de dados MySQL atrav�s das configura��es.
	 * Verifica primeiramente se a conex�o MySQL j� n�o foi instanciada e estabelecida.
	 * Em seguida obt�m as configura��es para conex�o e realiza o mesmo.
	 * @throws RagnarokException apenas se n�o conseguir conetar ou j� estiver conectado.
	 */

	private void initSqlConnection() throws RagnarokException
	{
		try {

			if (sql.getConnection() != null && !sql.getConnection().isClosed())
				throw new RagnarokException("conex�o j� estabelecida");

		} catch (SQLException e) {
			logWarning("falha ao verificar exist�ncia da conex�o MySQL");
		}

		String host = configs.getString("sql.host");
		String username = configs.getString("sql.username");
		String password = configs.getString("sql.password");
		String database = configs.getString("sql.database");
		int port = configs.getInt("sql.port");
		boolean legacy = configs.getBool("sql.legacydatetime");

		sql.setHost(host);
		sql.setUsername(username);
		sql.setPassword(password);
		sql.setDatabase(database);
		sql.setPort(port);
		sql.setUseLegacyDatetimeCode(legacy);
		sql.setServerTimezone("Africa/Abidjan");

		try {
			sql.connect();
		} catch (ClassNotFoundException e) {
			throw new RagnarokException("biblioteca MySQL Connector n�o encontrada");
		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		logNotice("conex�o MySQL estabelecida (%s:%d).\n", host, port);
	}

	/**
	 * Garante que o sistema de temporizadores seja inicializado.
	 */

	private void initTimer()
	{
		timerSystem.init();
	}

	/**
	 * Inicializa��o da thread respons�veis por receber conex�es socket dos clientes.
	 * Instancia a thread, define o nome e prioridade tal como a interface Runnable.
	 */

	private void initThreads()
	{
		Server self = this;

		threadSocket = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (state != DESTROYED)
				{
					if (state != RUNNING)
					{
						sleep(1000);
						continue;
					}

					try {

						Socket socket = serverSocket.accept();

						FileDescriptor fd = fileDescriptorSystem.newFileDecriptor(socket);
						fd.setParseListener(defaultParser);

					} catch (IOException e) {
						logExeception(e);
					}
				}

				Thread.interrupted();
			}

			@Override
			public String toString()
			{
				return self.toString();
			}
		});
		threadSocket.setName(getThreadName()+ "|ServerSocket");
		threadSocket.setPriority(Thread.MIN_PRIORITY);
		threadSocket.setDaemon(false);

		threadServer = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (state != DESTROYED)
				{
					if (state != RUNNING)
					{
						sleep(1000);
						continue;
					}

					int next = timerSystem.update(timerSystem.tick());
					fileDescriptorSystem.update(next);
				}
			}
		});
		threadServer.setName(getThreadName()+ "|Server");
		threadServer.setPriority(getThreadPriority());
		threadServer.setDaemon(false);

		logNotice("thread do servidor criada.\n");
	}

	/**
	 * Inicializa��o do servidor socket para receber as conex�es dos clientes.
	 * A conex�o � feita usado o endere�o do servidor e porta especificados.
	 * @throws RagnarokException apenas se n�o conseguir se conectar.
	 */

	private void initSocket() throws RagnarokException
	{
		try {

			int port = getPort();

			if (!IntUtil.interval(port, MIN_PORT, MAX_PORT))
				throw new RagnarokException("porta %d inv�lida");

			InetAddress address = InetAddress.getByName(getHost());
			serverSocket = new ServerSocket(port, SOCKET_BACKLOG, address);

			logNotice("conex�o estabelecida com �xito (porta: %d).\n", port);

		} catch (UnknownHostException e) {
			throw new RagnarokException("host desconhecido");
		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("state", state);
		description.append("host", getHost());
		description.append("port", getPort());
		description.append("thread", getThreadName());

		if (sql != null && sql.getConnection() != null)
			description.append("sql", sql.getDatabase());

		description.append("uptime", time(timerSystem.getUptime()));
		description.append("clients", fileDescriptorSystem.size());

		return description.toString();
	}
}

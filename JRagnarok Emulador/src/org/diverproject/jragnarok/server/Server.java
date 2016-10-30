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
import org.diverproject.util.lang.IntUtil;
import org.diverproject.util.lang.ShortUtil;
import org.diverproject.util.sql.MySQL;

/**
 * <h1>Servidor</h1>
 *
 * <p>Classe usada para definir um sistema de servidor com as informações e métodos para tal.
 * As informações referentes é de um ServerSocket para receber a conexão socket dos clientes.
 * Possui ainda um Thread para processar esses clientes recebidos e conexão com o banco de dados.</p>
 *
 * <p>Além do básico para um servidor, possui um listener de servidor implementado nas herdeiras.
 * Também possui um ServerConfig que irá armazenar todas as configurações pertinentes ao mesmo.
 * Por último determina um FileDescriptorListener que determina quem irá analisar os clientes.</p>
 *
 * @author Andrew
 */

public abstract class Server
{
	/**
	 * Porta mínima aceita pelos servidores.
	 */
	private static final int MIN_PORT = 1001;

	/**
	 * Porta máxima aceita pelos servidores.
	 */
	private static final int MAX_PORT = 65535;

	/**
	 * TODO what is that?
	 */
	private static final int SOCKET_BACKLOG = 50;


	/**
	 * Código de identificação do servidor de acesso.
	 */
	private int id;

	/**
	 * Thread que será usada para receber as conexões sockets.
	 */
	private Thread thread;

	/**
	 * Estado do servidor para garantir ordem nas chamadas.
	 */
	private ServerState state;

	/**
	 * Conexão socket de servidor para receber os clientes.
	 */
	private ServerSocket serverSocket;

	/**
	 * Listener usado pelas classes herdeiras afim de executar operações especificas.
	 */
	private ServerListener listener;

	/**
	 * Configurações do servidor.
	 */
	private Configurations configs;

	/**
	 * Listener para despachar os Arquivos Descritores.
	 */
	private FileDescriptorListener defaultParser;

	/**
	 * Conexão com o banco de dados MySQL.
	 */
	private MySQL sql;

	/**
	 * Cria um novo servidor definindo o servidor no estado NONE (nenhum/inicial).
	 * Também define as configurações do servidor por setServerConfig().
	 * E por fim instancia o objeto para criar a conexão com o banco de dados.
	 */

	public Server()
	{
		this.state = NONE;
		this.sql = new MySQL();
	}

	/**
	 * O código de identificação é individual para cada tipo de servidor.
	 * @return aquisição do código de identificação do servidor.
	 */

	public final int getID()
	{
		return id;
	}

	/**
	 * O código de identificação é individual para cada tipo de servidor.
	 * Acesso por pacote e utilizado por ServerControl quando listado.
	 * @param id código de identificação do servidor.
	 * @see ServerControl
	 */

	final void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisição do nome que será dado a thread do servidor.
	 */

	protected final String getThreadName()
	{
		return format("%s#%d@%s:%d", nameOf(this), id, getHost(), getPort());
	}

	/**
	 * @return aquisição do nível de prioridade da thread (MIN_PRIORITY ou MAX_PRIORITY).
	 */

	public int getThreadPriority()
	{
		int value = configs.getInt(SERVER_THREAD_PRIORITY);

		return IntUtil.limit(value, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
	}

	/**
	 * Nome do host permite definir a interface para recebimento das conexões.
	 * @return aquisição do host para realizar a conexão socket (ip ou domínio).
	 */

	public String getHost()
	{
		String value = configs.getString(SERVER_HOST);

		return value == null || value.isEmpty() ? "localhost" : value;
	}

	/**
	 * Através da porta é possível saber por onde as conexões são recebidas na máquina.
	 * @return aquisição da porta em que o servidor irá receber as conexões.
	 */

	public short getPort()
	{
		short port = s(configs.getInt(SERVER_PORT));

		return ShortUtil.min(port, s(1001));
	}

	/**
	 * O estado do servidor pode ser útil para realizar determinadas operações.
	 * As vezes é necessário que o servidor esteja ou não rodando para tal.
	 * @return aquisição do estado do servidor no momento.
	 */

	public ServerState getState()
	{
		return state;
	}

	/**
	 * Verifica se o servidor se encontra em um determinado estado especificado.
	 * @param state referência da enumeração do estado do qual deseja verificar.
	 * @return true se estiver no estado passado por parâmetro ou false caso contrário.
	 */

	public boolean isState(ServerState state)
	{
		return this.state.equals(state);
	}

	/**
	 * Permite definir qual será o listener usado para executar as operações nas trocas de estado.
	 * @param listener referência do objeto que implementou a interface desse listener.
	 */

	public void setListener(ServerListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Permite definir qual será o objeto usado para armazenar as configurações do servidor.
	 * Pode ser definido apenas uma única vez, logo se já tiver sido definido não terá efeito.
	 * @param configs referência do objeto contendo as configurações do servidor.
	 */

	protected void setServerConfig(Configurations configs)
	{
		if (this.configs == null && configs != null)
			this.configs = configs;
	}

	/**
	 * Permite obter a referência do objeto com as configurações do servidor.
	 * @return aquisição do objeto com as configurações do servidor.
	 */

	public Configurations getConfigs()
	{
		return configs;
	}

	/**
	 * O analisador padrão é usado para determinar o despache dos novos clientes.
	 * Toda nova conexão recebida será despachada para esse listener.
	 * @param defaultParser referência do objeto que implementa esse listener.
	 */

	public void setDefaultParser(FileDescriptorListener defaultParser)
	{
		this.defaultParser = defaultParser;
	}

	/**
	 * Uma conexão com o banco de dados MySQL é essencial para funcionamento do servidor.
	 * A conexão será realizada durante a criação do servidor e fechada na destruição.
	 * Utiliza as configurações do servidor para definir senha, usuário e banco de dados.
	 * @return aquisição do objeto que permite realizar a conexão com o banco de dados.
	 */

	public MySQL getMySQL()
	{
		return sql;
	}

	/**
	 * Procedimento interno que permite alterar o estado em que o servidor se encontra.
	 * Para que um estado seja alterado pode ser necessário encontrar-se em outro.
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
	 * Cria instâncias necessárias de objetos dos quais serão utilizados.
	 * Essas instâncias consistem em iniciar os valores padrões das configurações,
	 * Inicia o método em estado CREATE e passa ao final do método para CREATED.
	 * conectar-se com o banco de dados SQL, temporizadores, thread e socket.
	 * @throws RagnarokException quando houver problemas em uma inicialização.
	 */

	public final void create() throws RagnarokException
	{
		if (!isState(NONE) && !isState(DESTROYED))
			throw new RagnarokException("o servidor não pode dar create em %s", state);

		listener.onCreate();
		{
			initConfigs();
			initSqlConnection();
			initTimer();
			initThread();
			initSocket();
		}
		listener.onCreated();
		setNextState();
	}

	/**
	 * Ao ser chamado irá rodar o servidor, para isso irá trabalhar com a thread.
	 * A thread é iniciada ou retorna a rodar se estiver em STOPPED.
	 * Após rodar a thread e executar o listener irá entrar em RUNNING.
	 * @throws RagnarokException estado incorreto ou thread não criada.
	 */

	@SuppressWarnings("deprecation")
	public final void run() throws RagnarokException
	{
		if (!isState(CREATED))
			throw new RagnarokException("o servidor não pode dar run em %s", state);

		if (thread == null)
			throw new RagnarokException("thread não criada");

		listener.onRunning();
		{
			if (thread.isInterrupted())
				thread.resume();
			else
				thread.start();
		}

		setNextState();
	}

	/**
	 * Ao ser chamado irá parar o servidor, para isso interrompe a thread.
	 * Conexão com o banco de dados e server socket continuam ativas.
	 * Porém como a thread está inativa os clientes ficaram sem sinal.
	 * @throws RagnarokException erro no listener ou thread nula.
	 */

	public final void stop() throws RagnarokException
	{
		if (!isState(RUNNING))
			throw new RagnarokException("o servidor não pode dar stop em %s", state);

		if (thread == null)
			throw new RagnarokException("thread não encontrada");

		listener.onStop();
		{
			thread.interrupt();
		}
		setNextState();
	}

	/**
	 * Realiza a destruição do servidor, deve chamar a destruição dos outros objetos.
	 * A destruição consiste no listener que pode solicitar a limpeza de uma coleção.
	 * Fecha a conexão socket parando de receber novos clientes e interrompe a thread.
	 * Inicia o processo entrando no estado de DESTROY e termina como DESTROYED.
	 * @throws RagnarokException estado incorreto ou falha ao fechar conexão.
	 */

	public final void destroy() throws RagnarokException
	{
		if (!isState(STOPED))
			throw new RagnarokException("o servidor não pode dar destroy em %s", state);

		try {

			listener.onDestroy();
			serverSocket.close();

			state = ServerState.DESTROYED;

			listener.onDestroyed();

			thread.interrupt();
			thread = null;

		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Inicialização das configurações irá carregar todas as configurações necessárias.
	 * Para o servidor é considerado apenas configurações do banco de dados.
	 * @throws RagnarokException falha durante o carregamento das configurações.
	 */

	protected void initConfigs() throws RagnarokException
	{
		String filenames[] = configs.getString(SERVER_FILES).split(",");

		for (String filename : filenames)
		{
			String folder = configs.getString(SERVER_FOLDER);
			String filePath = format("config/Servers/%s%s", folder, filename.trim());

			readConfigFile(filePath);
		}

		filenames = configs.getString(SYSTEM_SERVER_DEFAULT_FILES).split(",");

		for (String filename : filenames)
		{
			String folder = configs.getString(SYSTEM_SERVER_DEFAULT_FOLDER);
			String filePath = format("config/Servers/%s%s", folder, filename.trim());

			readConfigFile(filePath);
		}
	}

	/**
	 * Efetua a leitura de um arquivo de configurações atualizando as configurações do servidor.
	 * @param filePath caminho completo ou parcial do arquivo de configurações.
	 */

	private void readConfigFile(String filePath)
	{
		ConfigReader load = new ConfigReader();
		load.setConfigurations(configs);
		load.setFilePath(filePath);

		try {

			logNotice("lido %d configurações de '%s'.\n", load.read(), filePath);

		} catch (RagnarokException e) {
			logError("falha ao ler '%s'.\n", filePath);
			logExeception(e);
		}
	}

	/**
	 * Inicialização da conexão com o banco de dados MySQL através das configurações.
	 * Verifica primeiramente se a conexão MySQL já não foi instanciada e estabelecida.
	 * Em seguida obtém as configurações para conexão e realiza o mesmo.
	 * @throws RagnarokException apenas se não conseguir conetar ou já estiver conectado.
	 */

	private void initSqlConnection() throws RagnarokException
	{
		try {

			if (sql.getConnection() != null && !sql.getConnection().isClosed())
				throw new RagnarokException("conexão já estabelecida");

		} catch (SQLException e) {
			logWarning("falha ao verificar existência da conexão MySQL");
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
			throw new RagnarokException("biblioteca MySQL Connector não encontrada");
		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		logNotice("conexão MySQL estabelecida (%s:%d).\n", host, port);
	}

	/**
	 * Garante que o sistema de temporizadores seja inicializado.
	 */

	private void initTimer()
	{
		TimerSystem timer = TimerSystem.getInstance();
		timer.init();
	}

	/**
	 * Inicialização da thread responsáveis por receber conexões socket dos clientes.
	 * Instancia a thread, define o nome e prioridade tal como a interface Runnable.
	 */

	private void initThread()
	{
		Server self = this;

		thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (state != DESTROYED)
				{
					if (state != RUNNING)
						sleep(1000);

					try {

						Socket socket = serverSocket.accept();

						FileDescriptor fd = FileDescriptor.newFileDecriptor(socket);
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
		thread.setName(getThreadName());
		thread.setPriority(getThreadPriority());
		thread.setDaemon(false);

		logNotice("thread do servidor criada.\n");
	}

	/**
	 * Inicialização do servidor socket para receber as conexões dos clientes.
	 * A conexão é feita usado o endereço do servidor e porta especificados.
	 * @throws RagnarokException apenas se não conseguir se conectar.
	 */

	private void initSocket() throws RagnarokException
	{
		try {

			int port = getPort();

			if (!IntUtil.interval(port, MIN_PORT, MAX_PORT))
				throw new RagnarokException("porta %d inválida");

			InetAddress address = InetAddress.getByName(getHost());
			serverSocket = new ServerSocket(port, SOCKET_BACKLOG, address);

			logNotice("conexão estabelecida com êxito (porta: %d).\n", port);

		} catch (UnknownHostException e) {
			throw new RagnarokException("host desconhecido");
		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newFileConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newLogConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newServerConfigs;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.newSqlConnectionConfigs;
import static org.diverproject.jragnarok.JRagnarokConstants.LOCALHOST;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_FILES;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SERVER_THREAD_PRIORITY;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_DATABASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_HOST;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_LEGACY_DATETIME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_TIMEZONE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SQL_USERNAME;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_DEFAULT_FOLDER;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.SYSTEM_SERVER_FOLDER;
import static org.diverproject.jragnarok.server.ServerState.CREATED;
import static org.diverproject.jragnarok.server.ServerState.DESTROYED;
import static org.diverproject.jragnarok.server.ServerState.NONE;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.ServerState.STOPED;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.b;
import static org.diverproject.util.Util.format;
import static org.diverproject.util.Util.nameOf;
import static org.diverproject.util.Util.sleep;
import static org.diverproject.util.Util.time;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;

import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.jragnarok.configuration.ConfigReader;
import org.diverproject.jragnarok.configuration.Configurations;
import org.diverproject.jragnarok.console.Show;
import org.diverproject.jragnarok.console.ShowThread;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.lang.Bits;
import org.diverproject.util.lang.IntUtil;
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
	private Thread threadSocket;

	/**
	 * Thread que será usada para manter o servidor rodando.
	 */
	private Thread threadServer;

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
	 * Conexão com o banco de dados MySQL.
	 */
	private MySQL sql;

	/**
	 * Sitema de temporização do servidor.
	 */
	private TimerSystem timerSystem;

	/**
	 * Sistema para criação de Descritor de Arquivos.
	 */
	private FileDescriptorSystem fileDescriptorSystem;

	/**
	 * Serviço para exibição de mensagens no console.
	 */
	private Show show;

	/**
	 * Cria um novo servidor definindo o servidor no estado NONE (nenhum/inicial).
	 * Também define as configurações do servidor por setServerConfig().
	 * E por fim instancia o objeto para criar a conexão com o banco de dados.
	 */

	public Server()
	{
		state = NONE;
		sql = new MySQL();
		timerSystem = new TimerSystem();
		fileDescriptorSystem = new FileDescriptorSystem(timerSystem);
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

	public final void setID(int id)
	{
		if (this.id == 0)
			this.id = id;
	}

	/**
	 * @return aquisição do serviço para exibição de mensagens no console.
	 */

	public Show getShow()
	{
		return show;
	}

	/**
	 * @return aquisição do nome que será dado a thread do servidor.
	 */

	public final String getThreadName()
	{
		return format("%s#%d@%s:%d", nameOf(this), id, getHost(), getPort());
	}

	/**
	 * @return aquisição do nível de prioridade da thread (MIN_PRIORITY ou MAX_PRIORITY).
	 */

	public int getThreadPriority()
	{
		int value = configs.getInt(SERVER_THREAD_PRIORITY);

		return IntUtil.min(value, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
	}

	/**
	 * @return aquisição do endereço de IP em que o servidor está conectado.
	 */

	public int getAddress()
	{
		if (serverSocket == null || serverSocket.isClosed())
			return Bits.makeInt(b(127), b(0), b(0), b(1));

		return SocketUtil.socketIPInt(serverSocket.getInetAddress().getHostAddress());
	}

	/**
	 * Nome do host permite definir a interface para recebimento das conexões.
	 * @return aquisição do host para realizar a conexão socket (ip ou domínio).
	 */

	public abstract String getHost();

	/**
	 * Através da porta é possível saber por onde as conexões são recebidas na máquina.
	 * @return aquisição da porta em que o servidor irá receber as conexões.
	 */

	public abstract int getPort();

	/**
	 * As configurações padrões identificam o nome dos arquivos que serão lidos por padrão.
	 * Após a leitura dos arquivos padrões é feito a leitura dos arquivos especificados que sobrepõe.
	 * @return aquisição da string contendo o nome de todos os arquivos padrões separados por vírgula.
	 */

	public abstract String getDefaultConfigs();

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
	 * Permite obter a referência do objeto com as configurações do servidor.
	 * @return aquisição do objeto com as configurações do servidor.
	 */

	protected Configurations getConfigs()
	{
		return configs;
	}

	/**
	 * Permite definir qual será o objeto usado para armazenar as configurações do servidor.
	 * Pode ser definido apenas uma única vez, logo se já tiver sido definido não terá efeito.
	 * @param configs referência do objeto contendo as configurações do servidor.
	 */

	public void setConfigurations(Configurations configs)
	{
		if (this.configs == null && configs != null)
			this.configs = configs;
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
	 * Os servidores possui um sistema de temporização individual para calcular os ticks.
	 * Cada tick represente um milissegundo no tempo real e influencia em chamados.
	 * @return aquisição do sistema para controle da temporização do servidor.
	 */

	public TimerSystem getTimerSystem()
	{
		return timerSystem;
	}

	/**
	 * Todo servidor precisa de um sistema que controle as sessões de conexões feitas.
	 * @return aquisição do sistema que efetua o controle das sessões no servidor.
	 */

	public FileDescriptorSystem getFileDescriptorSystem()
	{
		return fileDescriptorSystem;
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
			initThreads();
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

		if (threadSocket == null || threadServer == null)
			throw new RagnarokException("thread não criada");

		if (threadSocket.isInterrupted())
			threadSocket.resume();
		else
			threadSocket.start();

		if (threadServer.isInterrupted())
			threadServer.resume();
		else
			threadServer.start();

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

		if (threadSocket == null || threadServer == null)
			throw new RagnarokException("thread não encontrada");

		listener.onStop();
		{
			threadSocket.interrupt();
			threadServer.interrupt();
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
			{
				serverSocket.close();

				threadSocket.interrupt();
				threadServer.interrupt();
				threadSocket = null;
				threadServer = null;

				sql.closeConnection();
				configs.clear();
				timerSystem.destroy();
				fileDescriptorSystem.destroy();

				setNextState();
			}
			listener.onDestroyed();

		} catch (IOException | SQLException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Interface que será executada para manter o servidor recebendo novas conexões de clientes.
	 * Sempre que uma nova conexão for recebida irá registrado no sistema e despachá-lo a um listener.
	 * Caso o servidor não esteja sobrecarregado no próximo loop a thread do servidor irá processá-lo.
	 */

	private final Runnable THREAD_SOCKET_RUNNABLE = new Runnable()
	{
		@Override
		public void run()
		{
			try {

				initSocket();

			} catch (RagnarokException e) {

				logError("falha durante a inicialização do server socket:");
				logException(e);

				return;
			}

			while (state != DESTROYED)
			{
				if (serverSocket.isClosed() || state != RUNNING)
				{
					sleep(1000);
					continue;
				}

				try {

					Socket socket = serverSocket.accept();
					FileDescriptor fd = Server.this.acceptSocket(socket);

					if (fileDescriptorSystem.addFileDecriptor(fd))
						logDebug("nova conexão em '%s' (id: %d, ip: %s).\n", getThreadName(), fd.getID(), fd.getAddressString());

					if (fd == null)
						log("servidor está cheio, %s recusado.\n", SocketUtil.socketIP(socket));

				} catch (IOException e) {
					logException(e);
				}
			}

			Thread.interrupted();
		}

		@Override
		public String toString()
		{
			return Server.this.toString();
		}
	};

	/**
	 * Interface que será executada no momento em que a thread do servidor for inicializada.
	 * Essa thread será especifica para manter as informações do servidor e clientes atualizados.
	 */

	private final Runnable THREAD_SERVER_RUNNABLE = new Runnable()
	{
		@Override
		public void run()
		{
			Server.this.show = ShowThread.registerThread();

			try {

				initSqlConnection();
				initTimer();

				listener.onRunning();

			} catch (RagnarokException e) {

				logError("falha durante a inicialização do servidor:\n");
				logException(e);

				return;
			}

			while (state != DESTROYED)
			{
				if (state != RUNNING)
				{
					sleep(1000);
					continue;
				}

				int tick = timerSystem.tick();

				// TODO : Remover mais a frente quando gastar mais processamento?
				// Esperar ao menos 1ms para o próximo loop garantir ao menos 1 tick.
				if (tick == 0)
				{
					sleep(1);
					continue;
				}

				try {

					timerSystem.getTimers().update(timerSystem.getCurrentTime(), tick);
					fileDescriptorSystem.update(timerSystem.getCurrentTime(), tick);
					update(timerSystem.getCurrentTime(), tick);

				} catch (Exception e) {
					logException(e);
					e.printStackTrace();
				}
			}
		}

		@Override
		public String toString()
		{
			return Server.this.toString();
		}
	};

	/**
	 * A inicialização das configurações deverá carregar as configurações mínimas do servidor.
	 * Para tal será necessário carregar configurações básicas e de conexão com o banco de dados.
	 * @throws RagnarokException falha durante o carregamento das configurações.
	 */

	private void initConfigs() throws RagnarokException
	{
		Configurations fileConfigs = newFileConfigs();
		Configurations serverConfigs = newServerConfigs();
		Configurations sqlConfigs = newSqlConnectionConfigs();
		Configurations logConfigs = newLogConfigs();

		configs.add(fileConfigs);
		configs.add(serverConfigs);
		configs.add(sqlConfigs);
		configs.add(logConfigs);

		readConfigFiles();
	}

	/**
	 * Inicialização da thread responsáveis por receber conexões socket dos clientes.
	 * Instancia a thread, define o nome e prioridade tal como a interface Runnable.
	 */

	private void initThreads()
	{
		threadSocket = new Thread(THREAD_SOCKET_RUNNABLE);
		threadSocket.setName(getThreadName()+ "|ServerSocket");
		threadSocket.setPriority(Thread.MIN_PRIORITY);
		threadSocket.setDaemon(false);

		threadServer = new ServerThreaed(this, THREAD_SERVER_RUNNABLE);
		threadServer.setName(getThreadName()+ "|Server");
		threadServer.setPriority(getThreadPriority());
		threadServer.setDaemon(false);

		logInfo("thread do servidor criada.\n");
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

		String host = configs.getString(SQL_HOST);
		String username = configs.getString(SQL_USERNAME);
		String password = configs.getString(SQL_PASSWORD);
		String database = configs.getString(SQL_DATABASE);
		String timezone = configs.getString(SQL_TIMEZONE);

		int port = configs.getInt(SQL_PORT);
		boolean legacy = configs.getBool(SQL_LEGACY_DATETIME);

		sql.setHost(host);
		sql.setUsername(username);
		sql.setPassword(password);
		sql.setDatabase(database);
		sql.setPort(port);
		sql.setUseLegacyDatetimeCode(legacy);
		sql.setServerTimezone(timezone);

		try {
			sql.connect();
		} catch (ClassNotFoundException e) {
			throw new RagnarokException("biblioteca MySQL Connector não encontrada");
		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		logInfo("conexão MySQL estabelecida (%s:%d).\n", host, port);
	}

	/**
	 * Garante que o sistema de temporizadores seja inicializado.
	 */

	private void initTimer()
	{
		timerSystem.init();
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
				throw new RagnarokException("porta %d inválida", port);

			String host = getHost();

			if (host == null || host.isEmpty())
				host = LOCALHOST;

			InetAddress address = InetAddress.getByName(getHost());
			serverSocket = new ServerSocket(port, SOCKET_BACKLOG, address);

			logInfo("conexão estabelecida com êxito (porta: %d).\n", port);

		} catch (UnknownHostException e) {
			throw new RagnarokException("host desconhecido");
		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Faz a leitura todos os arquivos de configurações padrões e dos arquivos individuais do servidor.
	 * A leitura dos arquivos irá atualizar o valor das configurações que até então eram os padrões.
	 * Configurações lidas dos arquivos individuais do servidor sobrescrevem as dos arquivos padrões.
	 */

	private void readConfigFiles()
	{
		Configurations configs = getConfigs();

		ConfigReader reader = new ConfigReader();
		reader.getPreferences().set(ConfigReader.DEFAULT_PREFERENCES);
		reader.setConfigurations(configs);

		String serverFolder = configs.getString(SYSTEM_SERVER_FOLDER);

		String filepath = format("config/%s/%s%d.conf", serverFolder, getClass().getSimpleName(), getID());
		readConfigFileOf(reader, configs, filepath);

		readDefaultConfigs(configs, reader, serverFolder);
		readPrivateConfigs(configs, reader, serverFolder);
	}

	/**
	 * Efetua a leitura dos arquivos de configurações padrões para os servidores.
	 * Os arquivos padrões são carregados para qualquer servidor afim de ter um valor fixo.
	 * @param configs conjunto de configurações do servidor que terá os valores atualizados.
	 * @param reader leitor de configurações já configurado para carregar as informações.
	 * @param serverFolder diretório onde se encontra os servidores dentro das configurações.
	 */

	private void readDefaultConfigs(Configurations configs, ConfigReader reader, String serverFolder)
	{
		String defaultFolder = configs.getString(SYSTEM_SERVER_DEFAULT_FOLDER);
		String defaultFolderPath = format("config/%s/%s", serverFolder, defaultFolder);

		String defaultFilesConfig = getDefaultConfigs();
		String defaultFiles[] = defaultFilesConfig.split(",");

		for (String file : defaultFiles)
		{
			String filepath = format("%s/%s", defaultFolderPath, file);
			readConfigFileOf(reader, configs, filepath);
		}
	}

	/**
	 * Efetua a leitura dos arquivos de configurações privados dos servidores.
	 * Os arquivos privados são aplicados apenas a um servidor sobrescrevendo o padrão.
	 * @param configs conjunto de configurações do servidor que terá os valores atualizados.
	 * @param reader leitor de configurações já configurado para carregar as informações.
	 * @param serverFolder diretório onde se encontra os servidores dentro das configurações.
	 */

	private void readPrivateConfigs(Configurations configs, ConfigReader reader, String serverFolder)
	{
		String folder = configs.getString(SERVER_FOLDER);
		String folderPath = format("config/%s/%s", serverFolder, folder);

		String filesConfig = configs.getString(SERVER_FILES);
		String files[] = filesConfig.split(",");

		for (String file : files)
		{
			String filepath = format("%s/%s", folderPath, file);
			readConfigFileOf(reader, configs, filepath);
		}
	}

	/**
	 * Realiza a leitura de diversos arquivos em uma determinada pasta especificada.
	 * @param reader objeto que irá efetuar a leitura das configurações necessárias.
	 * @param configs objeto que está agrupando as configurações do servidor.
	 * @param folderPath caminho parcial ou completo da pasta que contém os arquivos.
	 * @param files vetor contendo o nome de todos os arquivos a serem lidos.
	 */

	private void readConfigFileOf(ConfigReader reader, Configurations configs, String filepath)
	{
		reader.setFilePath(filepath);

		try {
			reader.read();
		} catch (RagnarokException e) {
			logError("falha durante a leitura de '%s' (%s:%d).\n", filepath, nameOf(this), getID());
			logException(e);
		}

		Queue<RagnarokException> exceptions = reader.getExceptions();

		while (!exceptions.isEmpty())
			logWarning(exceptions.poll().getMessage()+ "\n");
	}

	/**
	 * Permite definir qual será o listener usado para executar as operações nas trocas de estado.
	 * @param listener referência do objeto que implementou a interface desse listener.
	 */

	protected void setListener(ServerListener listener)
	{
		this.listener = listener;
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

		logNotice("alterando estado de %s para %s.\n", old, state);
	}

	/**
	 * Procedimento chamado no momento em que uma conexão socket for recebida.
	 * Deverá criar um descritor de arquivo conforme as necessidades do servidor.
	 * @param socket nova conexão socket recebida pelo servidor.
	 * @return aquisição do descritor de arquivo referente a conexão socket.
	 */

	protected abstract FileDescriptor acceptSocket(Socket socket);

	/**
	 * Procedimento utilizado para permitir que os servidores implementem suas próprias atualizações.
	 * @param now horário atual do servidor, tempo em milissegundos que está rodando (online).
	 * @param tick intervalo entre a última chamada e esta chamada em milissegundos (delay).
	 */

	protected abstract void update(int now, int tick);

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

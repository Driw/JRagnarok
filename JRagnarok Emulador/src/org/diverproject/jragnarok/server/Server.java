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
	 * Servi�o para exibi��o de mensagens no console.
	 */
	private Show show;

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
	 * @see ServerControl
	 */

	public final void setID(int id)
	{
		if (this.id == 0)
			this.id = id;
	}

	/**
	 * @return aquisi��o do servi�o para exibi��o de mensagens no console.
	 */

	public Show getShow()
	{
		return show;
	}

	/**
	 * @return aquisi��o do nome que ser� dado a thread do servidor.
	 */

	public final String getThreadName()
	{
		return format("%s#%d@%s:%d", nameOf(this), id, getHost(), getPort());
	}

	/**
	 * @return aquisi��o do n�vel de prioridade da thread (MIN_PRIORITY ou MAX_PRIORITY).
	 */

	public int getThreadPriority()
	{
		int value = configs.getInt(SERVER_THREAD_PRIORITY);

		return IntUtil.min(value, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
	}

	/**
	 * @return aquisi��o do endere�o de IP em que o servidor est� conectado.
	 */

	public int getAddress()
	{
		if (serverSocket == null || serverSocket.isClosed())
			return Bits.makeInt(b(127), b(0), b(0), b(1));

		return SocketUtil.socketIPInt(serverSocket.getInetAddress().getHostAddress());
	}

	/**
	 * Nome do host permite definir a interface para recebimento das conex�es.
	 * @return aquisi��o do host para realizar a conex�o socket (ip ou dom�nio).
	 */

	public abstract String getHost();

	/**
	 * Atrav�s da porta � poss�vel saber por onde as conex�es s�o recebidas na m�quina.
	 * @return aquisi��o da porta em que o servidor ir� receber as conex�es.
	 */

	public abstract int getPort();

	/**
	 * As configura��es padr�es identificam o nome dos arquivos que ser�o lidos por padr�o.
	 * Ap�s a leitura dos arquivos padr�es � feito a leitura dos arquivos especificados que sobrep�e.
	 * @return aquisi��o da string contendo o nome de todos os arquivos padr�es separados por v�rgula.
	 */

	public abstract String getDefaultConfigs();

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
	 * Permite obter a refer�ncia do objeto com as configura��es do servidor.
	 * @return aquisi��o do objeto com as configura��es do servidor.
	 */

	protected Configurations getConfigs()
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
			initThreads();
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
	 * Interface que ser� executada para manter o servidor recebendo novas conex�es de clientes.
	 * Sempre que uma nova conex�o for recebida ir� registrado no sistema e despach�-lo a um listener.
	 * Caso o servidor n�o esteja sobrecarregado no pr�ximo loop a thread do servidor ir� process�-lo.
	 */

	private final Runnable THREAD_SOCKET_RUNNABLE = new Runnable()
	{
		@Override
		public void run()
		{
			try {

				initSocket();

			} catch (RagnarokException e) {

				logError("falha durante a inicializa��o do server socket:");
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
						logDebug("nova conex�o em '%s' (id: %d, ip: %s).\n", getThreadName(), fd.getID(), fd.getAddressString());

					if (fd == null)
						log("servidor est� cheio, %s recusado.\n", SocketUtil.socketIP(socket));

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
	 * Interface que ser� executada no momento em que a thread do servidor for inicializada.
	 * Essa thread ser� especifica para manter as informa��es do servidor e clientes atualizados.
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

				logError("falha durante a inicializa��o do servidor:\n");
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
				// Esperar ao menos 1ms para o pr�ximo loop garantir ao menos 1 tick.
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
	 * A inicializa��o das configura��es dever� carregar as configura��es m�nimas do servidor.
	 * Para tal ser� necess�rio carregar configura��es b�sicas e de conex�o com o banco de dados.
	 * @throws RagnarokException falha durante o carregamento das configura��es.
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
	 * Inicializa��o da thread respons�veis por receber conex�es socket dos clientes.
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
			throw new RagnarokException("biblioteca MySQL Connector n�o encontrada");
		} catch (SQLException e) {
			throw new RagnarokException(e.getMessage());
		}

		logInfo("conex�o MySQL estabelecida (%s:%d).\n", host, port);
	}

	/**
	 * Garante que o sistema de temporizadores seja inicializado.
	 */

	private void initTimer()
	{
		timerSystem.init();
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
				throw new RagnarokException("porta %d inv�lida", port);

			String host = getHost();

			if (host == null || host.isEmpty())
				host = LOCALHOST;

			InetAddress address = InetAddress.getByName(getHost());
			serverSocket = new ServerSocket(port, SOCKET_BACKLOG, address);

			logInfo("conex�o estabelecida com �xito (porta: %d).\n", port);

		} catch (UnknownHostException e) {
			throw new RagnarokException("host desconhecido");
		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}

	/**
	 * Faz a leitura todos os arquivos de configura��es padr�es e dos arquivos individuais do servidor.
	 * A leitura dos arquivos ir� atualizar o valor das configura��es que at� ent�o eram os padr�es.
	 * Configura��es lidas dos arquivos individuais do servidor sobrescrevem as dos arquivos padr�es.
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
	 * Efetua a leitura dos arquivos de configura��es padr�es para os servidores.
	 * Os arquivos padr�es s�o carregados para qualquer servidor afim de ter um valor fixo.
	 * @param configs conjunto de configura��es do servidor que ter� os valores atualizados.
	 * @param reader leitor de configura��es j� configurado para carregar as informa��es.
	 * @param serverFolder diret�rio onde se encontra os servidores dentro das configura��es.
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
	 * Efetua a leitura dos arquivos de configura��es privados dos servidores.
	 * Os arquivos privados s�o aplicados apenas a um servidor sobrescrevendo o padr�o.
	 * @param configs conjunto de configura��es do servidor que ter� os valores atualizados.
	 * @param reader leitor de configura��es j� configurado para carregar as informa��es.
	 * @param serverFolder diret�rio onde se encontra os servidores dentro das configura��es.
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
	 * @param reader objeto que ir� efetuar a leitura das configura��es necess�rias.
	 * @param configs objeto que est� agrupando as configura��es do servidor.
	 * @param folderPath caminho parcial ou completo da pasta que cont�m os arquivos.
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
	 * Permite definir qual ser� o listener usado para executar as opera��es nas trocas de estado.
	 * @param listener refer�ncia do objeto que implementou a interface desse listener.
	 */

	protected void setListener(ServerListener listener)
	{
		this.listener = listener;
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

		logNotice("alterando estado de %s para %s.\n", old, state);
	}

	/**
	 * Procedimento chamado no momento em que uma conex�o socket for recebida.
	 * Dever� criar um descritor de arquivo conforme as necessidades do servidor.
	 * @param socket nova conex�o socket recebida pelo servidor.
	 * @return aquisi��o do descritor de arquivo referente a conex�o socket.
	 */

	protected abstract FileDescriptor acceptSocket(Socket socket);

	/**
	 * Procedimento utilizado para permitir que os servidores implementem suas pr�prias atualiza��es.
	 * @param now hor�rio atual do servidor, tempo em milissegundos que est� rodando (online).
	 * @param tick intervalo entre a �ltima chamada e esta chamada em milissegundos (delay).
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

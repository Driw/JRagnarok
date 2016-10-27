package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.sleep;
import static org.diverproject.jragnarok.server.ServerState.CREATED;
import static org.diverproject.jragnarok.server.ServerState.DESTROYED;
import static org.diverproject.jragnarok.server.ServerState.NONE;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.ServerState.STOPED;
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
import org.diverproject.jragnaork.configuration.ConfigLoad;
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
	 * Thread que ser� usada para receber as conex�es sockets.
	 */
	private Thread thread;

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
	private ServerConfig configs;

	/**
	 * Listener para despachar os Arquivos Descritores.
	 */
	private FileDescriptorListener defaultParser;

	/**
	 * Conex�o com o banco de dados MySQL.
	 */
	private MySQL sql;

	/**
	 * Cria um novo servidor definindo o servidor no estado NONE (nenhum/inicial).
	 * Tamb�m define as configura��es do servidor por setServerConfig().
	 * E por fim instancia o objeto para criar a conex�o com o banco de dados.
	 * @throws RagnarokException
	 */

	public Server() throws RagnarokException
	{
		this.state = NONE;
		this.sql = new MySQL();
	}

	/**
	 * @return aquisi��o do nome que ser� dado a thread do servidor.
	 */

	protected abstract String getThreadName();

	/**
	 * @return aquisi��o do n�vel de prioridade da thread (MIN_PRIORITY ou MAX_PRIORITY).
	 */

	protected abstract int getThreadPriority();

	/**
	 * @return aquisi��o do host para realizar a conex�o socket (ip ou dom�nio).
	 */

	protected abstract String getAddress();

	/**
	 * @return aquisi��o da porta em que o servidor ir� receber as conex�es.
	 */

	protected abstract int getPort();

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

	public void setListener(ServerListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Permite definir qual ser� o objeto usado para armazenar as configura��es do servidor.
	 * Pode ser definido apenas uma �nica vez, logo se j� tiver sido definido n�o ter� efeito.
	 * @param configs refer�ncia do objeto contendo as configura��es do servidor.
	 */

	protected void setServerConfig(ServerConfig configs)
	{
		if (this.configs == null && configs != null)
			this.configs = configs;
	}

	/**
	 * Permite obter a refer�ncia do objeto com as configura��es do servidor.
	 * @return aquisi��o do objeto com as configura��es do servidor.
	 */

	public ServerConfig getConfigs()
	{
		return configs;
	}

	/**
	 * O analisador padr�o � usado para determinar o despache dos novos clientes.
	 * Toda nova conex�o recebida ser� despachada para esse listener.
	 * @param defaultParser refer�ncia do objeto que implementa esse listener.
	 */

	public void setDefaultParser(FileDescriptorListener defaultParser)
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
	 * Ao ser chamado ir� rodar o servidor, para isso ir� trabalhar com a thread.
	 * A thread � iniciada ou retorna a rodar se estiver em STOPPED.
	 * Ap�s rodar a thread e executar o listener ir� entrar em RUNNING.
	 * @throws RagnarokException
	 */

	@SuppressWarnings("deprecation")
	public final void run() throws RagnarokException
	{
		if (thread == null)
			throw new RagnarokException("thread n�o criada");

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
	 * Ao ser chamado ir� parar o servidor, para isso interrompe a thread.
	 * Conex�o com o banco de dados e server socket continuam ativas.
	 * Por�m como a thread est� inativa os clientes ficaram sem sinal.
	 * @throws RagnarokException erro no listener ou thread nula.
	 */

	public final void stop() throws RagnarokException
	{
		if (thread == null)
			throw new RagnarokException("thread n�o encontrada");

		listener.onStop();
		{
			thread.interrupt();
		}
		setNextState();
	}

	/**
	 * Realiza a destrui��o do servidor, deve chamar a destrui��o dos outros objetos.
	 * A destrui��o consiste no listener que pode solicitar a limpeza de uma cole��o.
	 * Fecha a conex�o socket parando de receber novos clientes e interrompe a thread.
	 * Inicia o processo entrando no estado de DESTROY e termina como DESTROYED.
	 * @throws RagnarokException
	 */

	public final void destroy() throws RagnarokException
	{
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
	 * Inicializa��o das configura��es ir� carregar todas as configura��es necess�rias.
	 * Para o servidor � considerado apenas configura��es do banco de dados.
	 * @throws RagnarokException falha durante o carregamento das configura��es.
	 */

	protected void initConfigs() throws RagnarokException
	{
		ConfigLoad load = new ConfigLoad();
		load.setConfigurations(configs.getMap());
		load.setFilePath("config/SqlConnection.conf");
		load.read();
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
		TimerSystem timer = TimerSystem.getInstance();
		timer.init();
	}

	/**
	 * Inicializa��o da thread respons�veis por receber conex�es socket dos clientes.
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

			InetAddress address = InetAddress.getByName(getAddress());
			serverSocket = new ServerSocket(port, SOCKET_BACKLOG, address);

			logNotice("conex�o estabelecida com �xito (porta: %d).\n", port);

		} catch (UnknownHostException e) {
			throw new RagnarokException("host desconhecido");
		} catch (IOException e) {
			throw new RagnarokException(e.getMessage());
		}
	}
}

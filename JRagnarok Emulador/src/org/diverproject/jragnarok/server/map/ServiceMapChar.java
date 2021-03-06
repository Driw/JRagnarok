package org.diverproject.jragnarok.server.map;

import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.seconds;

import java.io.IOException;
import java.net.Socket;

import org.diverproject.jragnarok.database.MapIndexes;
import org.diverproject.jragnarok.database.impl.MapIndex;
import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.inter.charmap.HZ_ResultMapServerConnection;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_MapServerConnection;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_NotifyUserCount;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_SendInformations;
import org.diverproject.jragnarok.packets.inter.mapchar.ZH_SendMaps;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerAdapt;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.util.collection.Queue;
import org.diverproject.util.collection.abstraction.DynamicQueue;

/**
 * <h1>Servi�o para Comunica��o com o Servidor de Personagem</h1>
 *
 * <p>Ap�s a identifica��o de uma solicita��o de conex�o com o servidor de acesso e que tenha sido autenticado,
 * caso a solicita��o tenha vindo de um servidor de personagem ser� passado para um analisador de pacotes que
 * ir� despachar redirecionar todas a��es recebidas dos pacotes para este servi�o que ir� operar as a��es.</p>
 *
 * <p>De modo geral todas as solicita��es s�o para que o servidor de acesso busque dados de uma conta.
 * Ap�s buscar esses dados eles s�o armazenados em pacotes e enviados de volta ao servidor de personagem.</p>
 *
 * @see AbstractMapService
 *
 * @author Andrew
 */

public class ServiceMapChar extends AbstractMapService
{
	/**
	 * Descritor de Arquivo para com o servidor de acesso.
	 */
	private MFileDescriptor fd;

	/**
	 * Servi�o para comunica��o com o cliente do servidor de mapa.
	 */
	private ServiceMapClient client;

	/**
	 * Mapeamento das autentica��es no servidor de mapas.
	 */
	private AuthMap auths;

	/**
	 * Indexa��o de todos os mapas do jogo.
	 */
	private MapIndexes maps;

	/**
	 * Determina ser o servi�o j� enviou as informa��es do servidor.
	 */
	private boolean sentInformations;

	/**
	 * Cria uma nova inst�ncia de um servi�o para comunica��o com um servidor de personagem.
	 * @param server refer�ncia do servidor de mapa que ir� utilizar o servi�o.
	 */

	public ServiceMapChar(MapServer server)
	{
		super(server);
	}

	/**
	 * @return aquisi��o do descritor de arquivo para com o servidor de acesso.
	 */

	private MFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getServiceMapClient();
		auths = getServer().getFacade().getAuthMap();
		maps = getServer().getFacade().getMapIndexes();

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer timerConnection = timers.acquireTimer();
		timerConnection.setListener(CHECK_CHAR_SERVER_CONNECTION);
		timerConnection.setTick(ts.getCurrentTime() + seconds(5));
		timers.addLoop(timerConnection, seconds(10));

		Timer timerAuthCleanup = timers.acquireTimer();
		timerAuthCleanup.setListener(AUTH_CLEANUP);
		timerAuthCleanup.setTick(ts.getCurrentTime() + seconds(1));
		timers.addLoop(timerAuthCleanup, seconds(30));

		Timer timerNotifyUserCount = timers.acquireTimer();
		timerNotifyUserCount.setListener(NOTIFY_USER_COUNT);
		timerNotifyUserCount.setTick(ts.getCurrentTime() + seconds(1));
		timers.addLoop(timerNotifyUserCount, seconds(10));
	}

	@Override
	public void destroy()
	{
		auths = null;
		maps = null;

		if (fd != null)
		{
			fd.close();
			fd = null;
		}
	}

	/**
	 * Verifica se o servidor de mapa desse servi�o possui conex�o com o servidor de personagem.
	 * Atrav�s dessa conex�o ser� poss�vel a comunica��o entre os dois servidores e troca de informa��es.
	 * @return true se a conex�o estiver estabelecida e conectada ou false caso contr�rio.
	 */

	public boolean isConnected()
	{
		return fd != null && fd.isConnected();
	}

	/**
	 * Verifica sa h� conex�o com o servidor de acesso para enviar os dados do pacote.
	 * Se houver envia os dados de um pacote para um determinado descritor de arquivo.
	 * Caso contr�rio envia ao cliente um pacote mostrando que este foi rejeitado.
	 * @param fd c�digo de identifica��o da conex�o do cliente com o servidor.
	 * @param packet pacote contendo os dados do qual ser�o enviados.
	 * @return true se houver a conex�o com o servidor de acesso ou false caso contr�rio.
	 */

	public boolean sendPacket(MFileDescriptor fd, IResponsePacket packet)
	{
		if (!isConnected())
		{
			//client.refuseEnter(fd, RE_REJECTED_FROM_SERVER);
			return false;
		}

		packet.send(getFileDescriptor());

		return true;
	}

	private final TimerAdapt CHECK_CHAR_SERVER_CONNECTION = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			// N�o usar isConnected() - se cair n�o conseguir� reconectar
			if (fd != null && fd.isConnected())
			{
				if (!sentInformations)
				{
					sendInformations(0, 0, 0); // TODO trocar pelas configura��es de batalha
					sentInformations = true;
				}

				return;
			}

			String host = config().charServerIP;
			short port = config().charServerPort;

			if (sentInformations)
				logInfo("tentando se reconectar com o servidor de personagem (%s:%d).\n", host, port);
			else
				logInfo("tentando se conectar com o servidor de personagem...\n");

			try {

				Socket socket = new Socket(host, port);

				MFileDescriptor fd = new MFileDescriptor(socket);
				fd.getFlag().set(MFileDescriptor.FLAG_SERVER);

				if (getFileDescriptorSystem().addFileDecriptor(fd))
				{
					String username = config().username;
					String password = config().password;
					int ipAddress = fd.getAddress();

					ZH_MapServerConnection packet = new ZH_MapServerConnection();
					packet.setUsername(username);
					packet.setPassword(password);
					packet.setIpAddress(ipAddress);
					packet.setPort(port);
					packet.send(fd);

					sentInformations = false;
				}

				fd.setParseListener(getServer().getFacade().PARSE_CHAR_SERVER);

			} catch (IOException e) {
				logInfo("falha ao conectar-se com %s:%d", host, port);
			}
		}

		@Override
		public String getName()
		{
			return "CHECK_CHAR_SERVER_CONNECTION";
		}
	};

	/**
	 * Listener que ir� limpar o mapeamento de autentica��es removendo os offline.
	 * Uma autentica��o ficar� offline se a conex�o fechar ou identificadores inv�lidos.
	 * Os identificadores inv�lidos s�o da conta do jogador ou do personagem selecionado.
	 */

	private final TimerAdapt AUTH_CLEANUP = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			auths.cleanup();
		}
		
		@Override
		public String getName()
		{
			return "AUTH_CLEANUP";
		}
	};

	/**
	 * Listener que ir� enviar um pacote ao servidor de personagem do qual est� conectado.
	 * Este pacote ir� informar a quantidade de personagens que est�o online no momento.
	 * O pacote � enviado em intervalos espec�ficos atrav�s de um temporizador.
	 */

	private final TimerAdapt NOTIFY_USER_COUNT = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			if (!isConnected())
				return;

			short userCOunt = 0; // TODO quantidade de jogadores online - map.c:map_usercount [174]

			logDebug("notificando %d jogadores online no servidor.\n", userCOunt);

			ZH_NotifyUserCount packet = new ZH_NotifyUserCount();
			packet.setUserCount(userCOunt);
			packet.send(getFileDescriptor());
		}

		@Override
		public String getName()
		{
			return "NOTIFY_USER_COUNT";
		}
	};

	/**
	 * Procedimento utilizado para atualizar a conex�o com o servidor de personagem.
	 * A atualiza��o consiste em verificar se h� conex�o e se n�o houver encerrar o objeto.
	 * @param now hor�rio atual do servidor, tempo em milissegundos que est� rodando (online).
	 * @param tick intervalo entre a �ltima chamada e esta chamada em milissegundos (delay).
	 */

	public void update(int now, int tick)
	{
		if (fd != null && !fd.isConnected())
		{
			fd.close();
			fd = null;
		}
	}

	/**
	 * Informa ao servidor de personagens as taxas do servidor das configura��es de batalha.
	 * Essas taxas s�o referentes a experi�ncia de base, experi�ncia de classe e itens derrubados.
	 * @param baseRate taxa percentual para a experi�ncia de base obtida de monstros.
	 * @param jobRate taxa percentual para a experi�ncia de classe obtida de monstros.
	 * @param dropRate taxa percentual para os itens (loot) derrubados por monstros.
	 */

	private void sendInformations(int baseRate, int jobRate, int dropRate)
	{
		ZH_SendInformations packet = new ZH_SendInformations();
		packet.setBaseRate(baseRate);
		packet.setJobRate(jobRate);
		packet.setDropRate(dropRate);
		packet.send(getFileDescriptor());
	}

	/**
	 * Recebe a notifica��o do resultado da tentativa de conex�o com o servidor de personagem.
	 * @param fd conex�o do descritor de arquivo do servidor de personagem com o servidor de mapa.
	 * @return true para manter a conex�o aberta ou false para fechar a conex�o.
	 */

	public boolean parseResultConnection(MFileDescriptor fd)
	{
		HZ_ResultMapServerConnection packet = new HZ_ResultMapServerConnection();
		packet.receive(fd);

		switch (packet.getResult())
		{
			case RMSC_FAILURE:
				logWarning("falha ao conectar-se com o servidor de personagem, verifique o usu�rio e senha.\n");
				return false;

			case RMSC_FULL:
				logWarning("muitos servidores mapa conectados a esse servidor de personagem (fd: %d, ip: %s).\n", fd.getID(), fd.getAddressString());
				return false;

			default:
		}

		logInfo("servidor de mapa conectado ao servidor de personagem (%s:%d).\n", fd.getAddressString(), config().port);
		sendMaps(this.fd = fd);

		// TODO continuar implementa��o de chrif.c:chrif_connectack [501]

		return true;
	}

	/**
	 * Informa ao servidor de personagem o �ndice de todos os mapas que est�o dispon�veis no servidor de mapas.
	 * Os mapas ser�o informados atrav�s de seus c�digos de identifica��o conforme carregados da base de dados.
	 * @param fd conex�o do descritor de arquivo do servidor de personagem com o servidor de mapa.
	 */

	private void sendMaps(MFileDescriptor fd)
	{
		logInfo("enviando mapas ao servidor de personagem...\n");

		Queue<MapIndex> indexes = new DynamicQueue<>();

		for (MapIndex map : maps)
			indexes.offer(map);

		ZH_SendMaps packet = new ZH_SendMaps();
		packet.setMaps(indexes);
		packet.send(fd);
	}

	/**
	 * Envia um pacote para um servidor de personagem para manter a conex�o estabelecida.
	 * Este procedimento � chamado ap�s o servidor de personagem solicitar um ping.
	 * @param fd conex�o do descritor de arquivo do servidor de personagem com o servidor de mapa.
	 */

	public void keepAlive(MFileDescriptor fd)
	{
		client.keepAliveCharServer(fd);
	}
}

package org.diverproject.jragnarok.server.map;

import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_IP;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CHAR_PORT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_PASSWORD;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.MAP_USERNAME;
import static org.diverproject.jragnarok.server.map.ServiceMapCharState.SMC_NONE;
import static org.diverproject.jragnarok.server.map.ServiceMapCharState.SMC_ONLINE;
import static org.diverproject.jragnarok.server.map.ServiceMapCharState.SMC_READY;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.s;

import java.io.IOException;
import java.net.Socket;

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
 * <h1>Serviço para Comunicação com o Servidor de Personagem</h1>
 *
 * <p>Após a identificação de uma solicitação de conexão com o servidor de acesso e que tenha sido autenticado,
 * caso a solicitação tenha vindo de um servidor de personagem será passado para um analisador de pacotes que
 * irá despachar redirecionar todas ações recebidas dos pacotes para este serviço que irá operar as ações.</p>
 *
 * <p>De modo geral todas as solicitações são para que o servidor de acesso busque dados de uma conta.
 * Após buscar esses dados eles são armazenados em pacotes e enviados de volta ao servidor de personagem.</p>
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
	 * Mapeamento das autenticações no servidor de mapas.
	 */
	private AuthMap auths;

	/**
	 * Determina ser o serviço já enviou as informações do servidor.
	 */
	private boolean sentInformations;

	/**
	 * Estado em que o serviço se encontra.
	 */
	private ServiceMapCharState state;

	/**
	 * Cria uma nova instância de um serviço para comunicação com um servidor de personagem.
	 * @param server referência do servidor de mapa que irá utilizar o serviço.
	 */

	public ServiceMapChar(MapServer server)
	{
		super(server);
	}

	/**
	 * @return aquisição do descritor de arquivo para com o servidor de acesso.
	 */

	private MFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	@Override
	public void init()
	{
		auths = getServer().getFacade().getAuthMap();

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		Timer timerConnection = timers.acquireTimer();
		timerConnection.setListener(CHECK_CHAR_SERVER_CONNECTION);
		timerConnection.setTick(ts.getCurrentTime() + seconds(1));
		timers.addInterval(timerConnection, seconds(10));

		Timer timerAuthCleanup = timers.acquireTimer();
		timerAuthCleanup.setListener(AUTH_CLEANUP);
		timerAuthCleanup.setTick(ts.getCurrentTime() + seconds(1));
		timers.addInterval(timerAuthCleanup, seconds(30));

		Timer timerNotifyUserCount = timers.acquireTimer();
		timerNotifyUserCount.setListener(NOTIFY_USER_COUNT);
		timerNotifyUserCount.setTick(ts.getCurrentTime() + seconds(1));
		timers.addInterval(timerNotifyUserCount, seconds(10));
	}

	@Override
	public void destroy()
	{
		auths = null;

		if (fd != null)
		{
			fd.close();
			fd = null;
		}
	}

	/**
	 * Verifica se o servidor de mapa desse serviço possui conexão com o servidor de personagem.
	 * Através dessa conexão será possível a comunicação entre os dois servidores e troca de informações.
	 * @return true se a conexão estiver estabelecida e conectada ou false caso contrário.
	 */

	public boolean isConnected()
	{
		return fd != null && fd.isConnected();
	}

	/**
	 * Verifica sa há conexão com o servidor de acesso para enviar os dados do pacote.
	 * Se houver envia os dados de um pacote para um determinado descritor de arquivo.
	 * Caso contrário envia ao cliente um pacote mostrando que este foi rejeitado.
	 * @param fd código de identificação da conexão do cliente com o servidor.
	 * @param packet pacote contendo os dados do qual serão enviados.
	 * @return true se houver a conexão com o servidor de acesso ou false caso contrário.
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
			if (isConnected())
			{
				if (!sentInformations)
				{
					sendInformations(0, 0, 0); // TODO trocar pelas configurações de batalha
					sentInformations = true;
				}

				return;
			}

			state = state == SMC_READY ? SMC_ONLINE : SMC_NONE;

			String host = getConfigs().getString(CHAR_IP);
			short port = s(getConfigs().getInt(CHAR_PORT));

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
					String username = getConfigs().getString(MAP_USERNAME);
					String password = getConfigs().getString(MAP_PASSWORD);
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
	 * Listener que irá limpar o mapeamento de autenticações removendo os offline.
	 * Uma autenticação ficará offline se a conexão fechar ou identificadores inválidos.
	 * Os identificadores inválidos são da conta do jogador ou do personagem selecionado.
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
	 * Listener que irá enviar um pacote ao servidor de personagem do qual está conectado.
	 * Este pacote irá informar a quantidade de personagens que estão online no momento.
	 * O pacote é enviado em intervalos específicos através de um temporizador.
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
	 * Informa ao servidor de personagens as taxas do servidor das configurações de batalha.
	 * Essas taxas são referentes a experiência de base, experiência de classe e itens derrubados.
	 * @param baseRate taxa percentual para a experiência de base obtida de monstros.
	 * @param jobRate taxa percentual para a experiência de classe obtida de monstros.
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
	 * Recebe a notificação do resultado da tentativa de conexão com o servidor de personagem.
	 * @param fd conexão do descritor de arquivo do servidor de personagem com o servidor de mapa.
	 * @return true para manter a conexão aberta ou false para fechar a conexão.
	 */

	public boolean parseResultConnection(MFileDescriptor fd)
	{
		HZ_ResultMapServerConnection packet = new HZ_ResultMapServerConnection();
		packet.receive(fd);

		switch (packet.getResult())
		{
			case RMSC_FAILURE:
				logWarning("falha ao conectar-se com o servidor de personagem, verifique o usuário e senha.\n");
				return false;

			case RMSC_FULL:
				logWarning("muitos servidores mapa conectados a esse servidor de personagem (fd: %d, ip: %s).\n", fd.getID(), fd.getAddressString());
				return false;

			default:
		}

		logInfo("servidor de mapa conectado ao servidor de personagem (%s:%d).\n", fd.getAddressString(), getConfigs().getInt(CHAR_PORT));

		state = SMC_ONLINE;
		sendMaps(fd);

		// TODO continuar implementação de chrif.c:chrif_connectack [501]

		return true;
	}

	/**
	 * Informa ao servidor de personagem o índice de todos os mapas que estão disponíveis no servidor de mapas.
	 * Os mapas serão informados através de seus códigos de identificação conforme carregados da base de dados.
	 * @param fd conexão do descritor de arquivo do servidor de personagem com o servidor de mapa.
	 */

	private void sendMaps(MFileDescriptor fd)
	{
		logInfo("enviando mapas ao servidor de personagem...\n");

		Queue<Integer> maps = new DynamicQueue<>();

		// TODO pegar o índice de todos os mapas que não sejam instâncias - 

		ZH_SendMaps packet = new ZH_SendMaps();
		packet.setMaps(maps);
		//packet.send(fd);
	}
}

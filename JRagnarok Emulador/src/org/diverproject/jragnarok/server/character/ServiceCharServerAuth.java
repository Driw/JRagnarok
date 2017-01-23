package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.server.common.DisconnectPlayer.DP_KICK_ONLINE;
import static org.diverproject.jragnarok.server.common.Sex.SERVER;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.character.fromclient.CH_Enter;
import org.diverproject.jragnarok.packets.common.NotifyAuth;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerAdapt;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.util.stream.Output;

/**
 * <h1>Serviço para Autenticação de Personagem</h1>
 *
 * <p>Neste serviço será feito toda a autenticação de um cliente logo que entra no servidor de personagem.
 * Irá validar as informações para garantir que seja o mesmo jogador utilizando a conta desde o servidor de acesso.
 * A autenticação consiste em solicitar as informações de seed (primeira/segunda), versão do cliente e afins.</p>
 *
 * <p>Um jogador só poderá prosseguir para a tela de seleção dos personagens apenas se aqui for autenticado.
 * A autenticação pode falhar por falha de conexão, outro jogador entrou ou já estava utilizando a mesma conta.
 * Também será responsável por solicitar ao servidor de acesso algumas informações da conta do jogador.</p>
 *
 * @see AbstractCharService
 * @see ServiceCharClient
 * @see ServiceCharServer
 * @see ServiceCharLogin
 * @see ServiceCharMap
 * @see AuthMap
 * @see OnlineControl
 *
 * @author Andrew
 */

public class ServiceCharServerAuth extends AbstractCharService
{
	/**
	 * Serviço para comunicação inicial com o cliente.
	 */
	private ServiceCharClient client;

	/**
	 * Serviço principal do servidor de personagem.
	 */
	private ServiceCharServer character;

	/**
	 * Serviço para comunicação com o servidor de acesso.
	 */
	private ServiceCharLogin login;

	/**
	 * Serviço para comunicação com o servidor de mapa.
	 */
	private ServiceCharMap map;

	/**
	 * Controle para autenticação de jogadores online.
	 */
	private AuthMap auths;

	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineControl onlines;

	/**
	 * Cria uma nova instância de um serviço para autenticação de clientes no servidor de personagem.
	 * @param server referência do servidor de personagem que irá utilizar este serviço.
	 */

	public ServiceCharServerAuth(CharServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getCharClient();
		character = getServer().getFacade().getCharService();
		login = getServer().getFacade().getLoginService();
		map = getServer().getFacade().getMapService();

		auths = getServer().getFacade().getAuthMap();
		onlines = getServer().getFacade().getOnlineControl();
	}

	@Override
	public void destroy()
	{
		client = null;
		character = null;
		login = null;
		map = null;

		auths = null;
		onlines = null;
	}

	/**
	 * Procedimento de chamada em um temporizador para remover um jogador online do sistema.
	 * Isso não irá notificar o jogador de que ficou online, apenas remover do sistema.
	 */

	private final TimerListener WAITING_DISCONNECT = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			OnlineCharData online = onlines.get(timer.getObjectID());
			online.setWaitingDisconnect(null);

			login.setCharOffline(timer.getObjectID(), online.getCharID());
		}
		
		@Override
		public String getName()
		{
			return "WAITING_DISCONNECT";
		}
	};

	/**
	 * Garante a autenticação de uma determinada sessão no sistema após selecionar este servidor de personagem.
	 * Caso uma sessão já tenha sido criada para o cliente deverá ignorar este chamado (não fazer nada).
	 * Caso contrário solicita dados ao servidor de acesso afim de garantir a autenticação no sistema.
	 * @param fd código de identificação do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conexão do cliente ou false para fechar sua conexão.
	 */

	public boolean parse(CFileDescriptor fd)
	{
		CH_Enter packet = new CH_Enter();
		packet.receive(fd);

		logNotice("conexão solicitada (aid: %d, seed: %d|%d).\n", packet.getAccountID(), packet.getFirstSeed(), packet.getSecondSeed());

		CharSessionData sd = fd.getSessionData();

		if (sd.getID() > 0)
		{
			logNotice("cliente já autenticado (aid: %d).\n", sd.getID());
			return true;
		}

		if (packet.getSex() == SERVER)
		{
			logNotice("cliente tentando se conectar como servidor (aid: %d).\n", sd.getID());
			return false;
		}

		sd.setID(packet.getAccountID());
		sd.getSeed().set(packet.getFirstSeed(), packet.getSecondSeed());
		sd.setSex(packet.getSex());
		sd.setAuth(false);

		try {

			Output output = fd.getPacketBuilder().newOutputPacket("SEND_BACK", 4);
			output.setInvert(true);
			output.putInt(sd.getID());
			output.flush();

		} catch (Exception e) {
			throw new RagnarokRuntimeException(e);
		}

		return parseAuthNode(fd);
	}

	/**
	 * Verifica se existe alguma autenticação da conta do cliente no sistema para ser utilizado.
	 * Se houver uma irá concluir a autenticação do cliente caso contrário faz a solicitação de uma.
	 * @param fd código de identificação do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conexão do cliente ou false para fechar sua conexão.
	 */

	private boolean parseAuthNode(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		AuthNode node = auths.get(sd.getID());

		if (node != null && node.getAccountID() == sd.getID() && node.getSeed().equals(sd.getSeed()))
		{
			sd.setVersion(node.getVersion());
			auths.remove(node.getAccountID());

			authOk(fd);
		}

		else
			return login.reqAuthAccount(fd);

		return true;
	}

	/**
	 * Realiza a autenticação de uma sessão do qual já se encontra online ou possuía uma autenticação.
	 * Em todo caso fecha a conexão referente a essa sessão existente e atualiza com a conexão abaixo:
	 * @param fd código de identificação do descritor de arquivo do cliente com o servidor.
	 */

	public boolean authOk(CFileDescriptor fd)
	{
		CharSessionData sd = fd.getSessionData();
		OnlineCharData online = onlines.get(sd.getID());

		if (online != null)
		{
			// Personagem online, dar kick do servidor
			if (online.getServer() > -1)
			{
				map.disconnectPlayer(fd, online.getCharID(), DP_KICK_ONLINE);

				if (online.getWaitingDisconnect() == null)
				{
					TimerSystem ts = getTimerSystem();
					TimerMap timers = getTimerSystem().getTimers();

					Timer timer = timers.acquireTimer();
					timer.setTick(ts.getCurrentTime());
					timer.setObjectID(online.getAccountID());
					timer.setListener(WAITING_DISCONNECT);
					timers.addInterval(timer, seconds(20));

					online.setWaitingDisconnect(timer);
				}

				client.sendNotifyResult(fd, NotifyAuth.NA_RECOGNIZES_LAST_LOGIN);
				return false;
			}

			// Já está conectado mas não selecionou um personagem
			if (online.getFileDescriptor() != null && online.getFileDescriptor().getID() != fd.getID())
			{
				client.sendNotifyResult(fd, NotifyAuth.NA_RECOGNIZES_LAST_LOGIN);
				return false;
			}

			online.setFileDescriptor(fd);
		}

		sd.setAuth(true);
		login.reqAccountData(fd);
		character.setCharSelectSection(fd);

		logDebug("atualizando autenticação encontrada (aid: %d).\n", sd.getID());

		return true;
	}
}

package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.server.common.DisconnectPlayer.KICK_ONLINE;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokRuntimeException;
import org.diverproject.jragnarok.packets.character.fromclient.CH_Enter;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.character.entities.AuthNode;
import org.diverproject.jragnarok.server.character.structures.CharSessionData;
import org.diverproject.jragnarok.server.character.structures.OnlineCharData;
import org.diverproject.jragnarok.server.common.NotifyAuthResult;
import org.diverproject.util.stream.Output;
import org.diverproject.util.stream.StreamException;

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
	private OnlineMap onlines;

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
		onlines = getServer().getFacade().getOnlineMap();
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

	private final TimerListener WAITING_DISCONNECT = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			OnlineCharData online = onlines.get(timer.getObjectID());
			onlines.remove(online);
		}
		
		@Override
		public String getName()
		{
			return "waitingDisconnect";
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

		sd.setID(packet.getAccountID());
		sd.getSeed().set(packet.getFirstSeed(), packet.getSecondSeed());
		sd.setAuth(false);

		try {

			Output output = fd.getPacketBuilder().newOutputPacket("CS_SELECTED_BACK", 4);
			output.putInt(packet.getAccountID());
			output.flush();
			output = null;

		} catch (StreamException e) {
			throw new RagnarokRuntimeException(e.getMessage());
		}

		return parseAuthAccount(fd);
	}

	/**
	 * Verifica se existe alguma autenticação da conta do cliente no sistema para ser utilizado.
	 * Se houver uma irá concluir a autenticação do cliente caso contrário faz a solicitação de uma.
	 * @param fd código de identificação do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conexão do cliente ou false para fechar sua conexão.
	 */

	private boolean parseAuthAccount(CFileDescriptor fd)
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
				map.disconnectPlayer(fd, online.getCharID(), KICK_ONLINE);

				if (online.getWaitingDisconnect() == null)
				{
					TimerMap timers = getTimerSystem().getTimers();

					Timer timer = timers.acquireTimer();
					timer.setListener(WAITING_DISCONNECT);
					timer.setObjectID(online.getAccountID());
					timers.addInterval(timer, seconds(20));

					online.setWaitingDisconnect(timer);
				}

				client.sendNotifyResult(fd, NotifyAuthResult.RECOGNIZES_LAST_LOGIN);
				return false;
			}

			// Já está conectado mas não selecionou um personagem
			if (online.getFileDescriptor() != null && online.getFileDescriptor().getID() != fd.getID())
			{
				client.sendNotifyResult(fd, NotifyAuthResult.RECOGNIZES_LAST_LOGIN);
				return false;
			}

			online.setFileDescriptor(fd);
		}

		sd.setAuth(true);
		login.reqAccountData(fd);
		character.setCharSelect(fd);

		logDebug("atualizando autenticação encontrada (aid: %d).\n", sd.getID());

		return true;
	}
}

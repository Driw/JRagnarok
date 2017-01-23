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
 * <h1>Servi�o para Autentica��o de Personagem</h1>
 *
 * <p>Neste servi�o ser� feito toda a autentica��o de um cliente logo que entra no servidor de personagem.
 * Ir� validar as informa��es para garantir que seja o mesmo jogador utilizando a conta desde o servidor de acesso.
 * A autentica��o consiste em solicitar as informa��es de seed (primeira/segunda), vers�o do cliente e afins.</p>
 *
 * <p>Um jogador s� poder� prosseguir para a tela de sele��o dos personagens apenas se aqui for autenticado.
 * A autentica��o pode falhar por falha de conex�o, outro jogador entrou ou j� estava utilizando a mesma conta.
 * Tamb�m ser� respons�vel por solicitar ao servidor de acesso algumas informa��es da conta do jogador.</p>
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
	 * Servi�o para comunica��o inicial com o cliente.
	 */
	private ServiceCharClient client;

	/**
	 * Servi�o principal do servidor de personagem.
	 */
	private ServiceCharServer character;

	/**
	 * Servi�o para comunica��o com o servidor de acesso.
	 */
	private ServiceCharLogin login;

	/**
	 * Servi�o para comunica��o com o servidor de mapa.
	 */
	private ServiceCharMap map;

	/**
	 * Controle para autentica��o de jogadores online.
	 */
	private AuthMap auths;

	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineControl onlines;

	/**
	 * Cria uma nova inst�ncia de um servi�o para autentica��o de clientes no servidor de personagem.
	 * @param server refer�ncia do servidor de personagem que ir� utilizar este servi�o.
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
	 * Isso n�o ir� notificar o jogador de que ficou online, apenas remover do sistema.
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
	 * Garante a autentica��o de uma determinada sess�o no sistema ap�s selecionar este servidor de personagem.
	 * Caso uma sess�o j� tenha sido criada para o cliente dever� ignorar este chamado (n�o fazer nada).
	 * Caso contr�rio solicita dados ao servidor de acesso afim de garantir a autentica��o no sistema.
	 * @param fd c�digo de identifica��o do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conex�o do cliente ou false para fechar sua conex�o.
	 */

	public boolean parse(CFileDescriptor fd)
	{
		CH_Enter packet = new CH_Enter();
		packet.receive(fd);

		logNotice("conex�o solicitada (aid: %d, seed: %d|%d).\n", packet.getAccountID(), packet.getFirstSeed(), packet.getSecondSeed());

		CharSessionData sd = fd.getSessionData();

		if (sd.getID() > 0)
		{
			logNotice("cliente j� autenticado (aid: %d).\n", sd.getID());
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
	 * Verifica se existe alguma autentica��o da conta do cliente no sistema para ser utilizado.
	 * Se houver uma ir� concluir a autentica��o do cliente caso contr�rio faz a solicita��o de uma.
	 * @param fd c�digo de identifica��o do descritor de arquivo do cliente com o servidor.
	 * @return true para manter a conex�o do cliente ou false para fechar sua conex�o.
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
	 * Realiza a autentica��o de uma sess�o do qual j� se encontra online ou possu�a uma autentica��o.
	 * Em todo caso fecha a conex�o referente a essa sess�o existente e atualiza com a conex�o abaixo:
	 * @param fd c�digo de identifica��o do descritor de arquivo do cliente com o servidor.
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

			// J� est� conectado mas n�o selecionou um personagem
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

		logDebug("atualizando autentica��o encontrada (aid: %d).\n", sd.getID());

		return true;
	}
}

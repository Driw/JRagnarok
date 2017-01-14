package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.seconds;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_ONLINE_CLEANUP_INTERVAL;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnarok.packets.inter.loginchar.AH_AlreadyOnline;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerAdapt;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.entities.Account;

/**
 * <h1>Servi�o de Acesso</h1>
 *
 * <p>Esse servi�o ir� receber uma solicita��o de conex�o do servi�o de acesso para clientes.
 * Essa solicita��o dever� ser autenticada conforme os dados da conta passados pelo cliente.
 * Os dados da conta ser�o obtidas dentro desse servi�o durante a autentica��o.</p>
 *
 * <p>Consistindo em realizar as seguintes autentica��es: nome de usu�rio com a palavra chave (senha),
 * tempo de para expira��o da conta, tempo restante de banimento, vers�o do cliente e client hash.
 * Para a autentica��o de tempo de banimento ser� considerado independente de configura��es</p>
 *
 * @see AbstractServiceLogin
 * @see AccountControl
 * @see OnlineMap
 * @see FileDescriptor
 *
 * @author Andrew Mello
 */

public class ServiceLoginServer extends AbstractServiceLogin
{
	/**
	 * Tempo para que uma autentica��o entre em timeout.
	 */
	private static final int AUTH_TIMEOUT = seconds(10);


	/**
	 * Servi�o para comunica��o entre o servidor e o cliente.
	 */
	private ServiceLoginClient client;

	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineMap onlines;

	/**
	 * Constr�i um novo servi�o para realizar a autentica��o e obten��o de contas.
	 * @param server refer�ncia do servidor de acesso do qual deseja criar o servi�o.
	 */

	public ServiceLoginServer(LoginServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getClientService();
		onlines = getServer().getFacade().getOnlineMap();

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		int interval = getConfigs().getInt(LOGIN_ONLINE_CLEANUP_INTERVAL);

		Timer timer = timers.acquireTimer();
		timer.setListener(ONLINE_DATA_CLEANUP);
		timer.setTick(ts.getCurrentTime() + seconds(interval));
		timers.addLoop(timer, seconds(interval));
	}

	@Override
	public void destroy()
	{
		client = null;
		onlines = null;
	}

	/**
	 * Fun��o para remover informa��es de jogadores online que n�o est�o em um servidor de personagem v�lido.
	 */

	private final TimerListener ONLINE_DATA_CLEANUP = new  TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			for (OnlineLogin online : onlines)
				if (online.getCharServerID() == OnlineLogin.CHAR_SERVER_OFFLINE)
					removeOnlineUser(online.getAccountID());
		}
		
		@Override
		public String getName()
		{
			return "ONLINE_DATA_CLEANUP";
		}
	};

	/**
	 * Fun��o para temporizadores executarem a remo��o de uma conta como acesso online.
	 */

	public final TimerListener WAITING_DISCONNECT_TIMER = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			int accountID = timer.getObjectID();
			OnlineLogin online = onlines.get(accountID);

			if (online == null)
				getTimerSystem().getTimers().delete(timer);
			else
			{
				onlines.remove(accountID);

				if (online.getWaitingDisconnect() != null)
				{
					getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
					online.setWaitingDisconnect(null);
				}
			}
		}

		@Override
		public String getName()
		{
			return "WAITING_DISCONNECT_TIMER";
		}
	};

	/**
	 * Verifica se uma determinada conta j� est� sendo utilizada no servidor de acesso.
	 * @param accountID c�digo de identifica��o da conta do qual deseja verificar.
	 * @return true se estiver sendo utilizada por um jogador ou false caso contr�rio.
	 */

	public boolean isOnline(int accountID)
	{
		return onlines.get(accountID) != null;
	}

	/**
	 * Autentica um cliente verificando se h� um outro cliente usando a conta acessada.
	 * Caso haja um cliente usado a conta, dever� avisar quem est� online e rejeitar o acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @return true se n�o houver ningu�m na conta online ou false caso contr�rio.
	 */

	public boolean isOnline(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();
		Account account = (Account) sd.getCache();
		OnlineLogin online = onlines.get(account.getID());

		if (online != null)
		{
			int id = online.getCharServerID();

			CharServerList servers = getServer().getCharServerList();
			ClientCharServer server = servers.get(id);

			if (server != null)
				accountIsOnline(fd, account, online, server);

			removeOnlineUser(online.getAccountID());
			return false;
		}

		return true;
	}

	/**
	 * A autentica��o do cliente indicou que a conta j� est� sendo usada (online).
	 * Notificar ao cliente de que a conta que o servidor ainda o considera online.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param account objeto contendo os detalhes da conta que est� sendo acessada.
	 * @param online objeto que cont�m o gatilho para efetuar logout for�ado.
	 * @param server servidor de personagens do qual a conta est� online.
	 */

	private void accountIsOnline(LFileDescriptor fd, Account account, OnlineLogin online, ClientCharServer server)
	{
		logNotice("usu�rio '%s' j� est� online em '%s'.\n", account.getUsername(), server.getName());

		AH_AlreadyOnline packet = new AH_AlreadyOnline();
		packet.setAccountID(account.getID());

		client.broadcast(fd, packet);

		if (online.getWaitingDisconnect() == null)
		{
			TimerSystem ts = getTimerSystem();
			TimerMap timers = ts.getTimers();

			Timer timer = timers.acquireTimer();
			timer.setTick(ts.getCurrentTime());
			timer.setObjectID(account.getID());
			timer.setListener(WAITING_DISCONNECT_TIMER);
			timers.addInterval(timer, AUTH_TIMEOUT);
		}
	}

	/**
	 * Adiciona um determinado usu�rio (conta de jogador) online do sistema atrav�s da sua identifica��o.
	 * Caso j� haja um temporizador definido ir� substitu�-lo por um novo e excluindo o antigo.
	 * Caso n�o seja encontrado informa��es desta conta como online, o m�todo n�o ter� nenhum efeito.
	 * @param charServerID c�digo de identifica��o do servidor de personagem no sistema.
	 * @param accountID c�digo de identifica��o da conta que ser� definida como online.
	 * @return aquisi��o do objeto contendo as informa��es referentes a conta online.
	 */

	public OnlineLogin addOnlineUser(int charServerID, int accountID)
	{
		OnlineLogin online = onlines.get(accountID);

		if (online == null)
		{
			online = new OnlineLogin();
			online.setAccountID(accountID);
			onlines.add(online);
		}

		if (online.getWaitingDisconnect() != null)
		{
			getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		online.setCharServer(charServerID);

		return online;
	}

	/**
	 * Remove um determinado usu�rio (conta de jogador) online do sistema atrav�s da sua identifica��o.
	 * Al�m da remo��o de suas informa��es do sistema remove as informa��es do seu temporizador.
	 * Caso n�o seja encontrado informa��es desta conta como online, o m�todo n�o ter� nenhum efeito.
	 * @param accountID c�digo de identifica��o da conta do qual deseja tornar online.
	 */

	public void removeOnlineUser(int accountID)
	{
		OnlineLogin online = onlines.get(accountID);

		if (online != null)
		{
			onlines.remove(accountID);

			if (online.getWaitingDisconnect() != null)
			{
				getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
				online.setWaitingDisconnect(null);
			}
		}
	}

	/**
	 * Define todos os usu�rios (conta de jogadores) como offline no sistema atrav�s do servidor de personagem.
	 * Caso seja passado <code>NO_CHAR_SERVER</code> ir� aplicar a todos os jogadores online ou n�o.
	 * @param charServerID c�digo de identifica��o do servidor de personagem ou <code>NO_CHAR_SERVER</code>.
	 * @return aquisi��o da quantidade de jogadores que foram afetados e ficaram como offline.
	 */

	public int setOfflineUser(int charServerID)
	{
		int offlines = 0;

		for (OnlineLogin online : onlines)
		{
			if (charServerID == OnlineLogin.NO_CHAR_SERVER)
			{
				getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
				online.setWaitingDisconnect(null);
				online.setCharServer(charServerID);
				offlines++;
			}

			else if (charServerID == online.getCharServerID())
			{
				online.setCharServer(OnlineLogin.CHAR_SERVER_OFFLINE);
				offlines++;
			}
		}

		return offlines;
	}

	/**
	 * 
	 * @param ip
	 * @return
	 */

	public int parseSubnetIP(int ip)
	{
		// TODO lan_subnetcheck
		return 0;
	}

	/**
	 * 
	 * @param firstPass
	 * @param secondPass
	 * @param password
	 * @return
	 */

	public boolean parseEncryptedPassword(String firstPass, String secondPass, String password)
	{
		// TODO login_check_encrypted
		return false;
	}
}

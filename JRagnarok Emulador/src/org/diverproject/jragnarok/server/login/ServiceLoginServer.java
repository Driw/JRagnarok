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
 * <h1>Serviço de Acesso</h1>
 *
 * <p>Esse serviço irá receber uma solicitação de conexão do serviço de acesso para clientes.
 * Essa solicitação deverá ser autenticada conforme os dados da conta passados pelo cliente.
 * Os dados da conta serão obtidas dentro desse serviço durante a autenticação.</p>
 *
 * <p>Consistindo em realizar as seguintes autenticações: nome de usuário com a palavra chave (senha),
 * tempo de para expiração da conta, tempo restante de banimento, versão do cliente e client hash.
 * Para a autenticação de tempo de banimento será considerado independente de configurações</p>
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
	 * Tempo para que uma autenticação entre em timeout.
	 */
	private static final int AUTH_TIMEOUT = seconds(10);


	/**
	 * Serviço para comunicação entre o servidor e o cliente.
	 */
	private ServiceLoginClient client;

	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineMap onlines;

	/**
	 * Constrói um novo serviço para realizar a autenticação e obtenção de contas.
	 * @param server referência do servidor de acesso do qual deseja criar o serviço.
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
	 * Função para remover informações de jogadores online que não estão em um servidor de personagem válido.
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
	 * Função para temporizadores executarem a remoção de uma conta como acesso online.
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
	 * Verifica se uma determinada conta já está sendo utilizada no servidor de acesso.
	 * @param accountID código de identificação da conta do qual deseja verificar.
	 * @return true se estiver sendo utilizada por um jogador ou false caso contrário.
	 */

	public boolean isOnline(int accountID)
	{
		return onlines.get(accountID) != null;
	}

	/**
	 * Autentica um cliente verificando se há um outro cliente usando a conta acessada.
	 * Caso haja um cliente usado a conta, deverá avisar quem está online e rejeitar o acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de acesso.
	 * @return true se não houver ninguém na conta online ou false caso contrário.
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
	 * A autenticação do cliente indicou que a conta já está sendo usada (online).
	 * Notificar ao cliente de que a conta que o servidor ainda o considera online.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de acesso.
	 * @param account objeto contendo os detalhes da conta que está sendo acessada.
	 * @param online objeto que contém o gatilho para efetuar logout forçado.
	 * @param server servidor de personagens do qual a conta está online.
	 */

	private void accountIsOnline(LFileDescriptor fd, Account account, OnlineLogin online, ClientCharServer server)
	{
		logNotice("usuário '%s' já está online em '%s'.\n", account.getUsername(), server.getName());

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
	 * Adiciona um determinado usuário (conta de jogador) online do sistema através da sua identificação.
	 * Caso já haja um temporizador definido irá substituí-lo por um novo e excluindo o antigo.
	 * Caso não seja encontrado informações desta conta como online, o método não terá nenhum efeito.
	 * @param charServerID código de identificação do servidor de personagem no sistema.
	 * @param accountID código de identificação da conta que será definida como online.
	 * @return aquisição do objeto contendo as informações referentes a conta online.
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
	 * Remove um determinado usuário (conta de jogador) online do sistema através da sua identificação.
	 * Além da remoção de suas informações do sistema remove as informações do seu temporizador.
	 * Caso não seja encontrado informações desta conta como online, o método não terá nenhum efeito.
	 * @param accountID código de identificação da conta do qual deseja tornar online.
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
	 * Define todos os usuários (conta de jogadores) como offline no sistema através do servidor de personagem.
	 * Caso seja passado <code>NO_CHAR_SERVER</code> irá aplicar a todos os jogadores online ou não.
	 * @param charServerID código de identificação do servidor de personagem ou <code>NO_CHAR_SERVER</code>.
	 * @return aquisição da quantidade de jogadores que foram afetados e ficaram como offline.
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

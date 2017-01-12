package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.now;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_BANNED_UNTIL;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_EXE_LASTED_VERSION;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_EXPIRED;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_INCORRECT_PASSWORD;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_OK;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_UNREGISTERED_ID;
import static org.diverproject.jragnarok.server.login.entities.AccountState.NONE;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnarok.packets.common.RefuseLogin;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.collection.Node;

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
	 * Controle para persistência das contas de jogadores.
	 */
	private AccountControl accounts;

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
		accounts = getServer().getFacade().getAccountControl();
		onlines = getServer().getFacade().getOnlineMap();
	}

	@Override
	public void destroy()
	{
		accounts = null;
		onlines = null;
	}

	/**
	 * Função para remover informações de jogadores online que não estão em um servidor de personagem válido.
	 */

	public final TimerListener ONLINE_DATA_CLEANUP = new  TimerListener()
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
			return "onlineDataCleanup";
		}
	};

	/**
	 * Procedimento chamado quando o serviço de identificação do cliente tiver autenticado o mesmo.
	 * Aqui deverá ser autenticado os dados que foram passados pelo cliente em relação a uma conta.
	 * Deverá garantir primeiramente que o nome de usuário é válido para se fazer um acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return resultado da autenticação da solicitação para acesso de uma conta.
	 */

	public RefuseLogin parseAuthLogin(LFileDescriptor fd, boolean server)
	{
		RefuseLogin result = null;
		LoginSessionData sd = fd.getSessionData();

		if ((result = authClientVersion(sd)) != RL_OK)
			return result;

		if ((result = makeLoginAccount(fd, server)) != RL_OK)
			return result;

		Account account = (Account) sd.getCache();

		logNotice("autenticação aceita (id: %d, username: %s, ip: %s).\n", account.getID(), account.getUsername(), fd.getAddressString());

		sd.setID(account.getID());
		sd.getLastLogin().set(account.getLastLogin().get());
		sd.setGroup(account.getGroup().getCurrentGroup());
		sd.setSex(account.getSex());

		sd.getSeed().genFirst();
		sd.getSeed().genSecond();

		account.getLastLogin().set(now());
		account.getLastIP().set(fd.getAddress());
		account.setLoginCount(account.getLoginCount() + 1);

		if (!accounts.set(account))
			logError("falha ao persistir conta (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());

		return RL_OK;
	}

	/**
	 * Comunica-se com o controle de contas para obter todos os dados da conta desejada.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param server true se o cliente for um servidor ou false se for um jogador.
	 * @return resultado da obtenção dos dados da conta que o cliente passou,
	 * caso os dados tenham sido obtidos com êxito ficaram no cache do FileDescriptor.
	 */

	private RefuseLogin makeLoginAccount(LFileDescriptor fd, boolean server)
	{
		LoginSessionData sd = fd.getSessionData();
		Account account = accounts.get(sd.getUsername());

		if (account == null)
		{
			logNotice("usuário não encontrado (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return RL_UNREGISTERED_ID;
		}

		RefuseLogin result = RefuseLogin.RL_OK;

		if ((result = authPassword(fd, account)) != RL_OK)
			return result;

		if ((result = authExpirationTime(fd, account)) != RL_OK)
			return result;

		if ((result = authBanTime(fd, account)) != RL_OK)
			return result;

		if ((result = authAccountState(fd, account)) != RL_OK)
			return result;

		if ((result = authClientHash(fd, account, server)) != RL_OK)
			return result;

		sd.setCache(account);

		return RL_OK;
	}

	/**
	 * Verifica primeiramente se está habilitado a verificação para versão do cliente.
	 * Caso esteja habilitado a versão do cliente deverá ser igual a da configuração definida.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @return resultado da autenticação da versão que o cliente está usando.
	 */

	private RefuseLogin authClientVersion(LoginSessionData sd)
	{
		if (getConfigs().getBool("client.check_version"))
		{
			int version = getConfigs().getInt("client.version");

			if (sd.getVersion() != version)
			{
				logNotice("versão inválida (account: %s, version (client/server): %d/%d).\n", sd.getUsername(), sd.getVersion(), version);
				return RL_EXE_LASTED_VERSION;
			}
		}

		return RL_OK;
	}

	/**
	 * Autentica se a senha passada pelo cliente corresponde com a senha da conta acessada.
	 * Caso não sejam iguais o cliente receberá uma mensagem de que a conta não pode ser acessada.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autenticação da senha passada pelo cliente com a da conta.
	 */

	private RefuseLogin authPassword(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();
		String password = account.getPassword();

		if (!sd.getPassword().equals(password))
		{
			logNotice("senha incorreta (username: %s, password: %s, receive pass: %s, ip: %s).\n", sd.getUsername(), sd.getPassword(), password, fd.getAddressString());
			return RL_INCORRECT_PASSWORD;
		}

		return RL_OK;
	}

	/**
	 * Autentica o tempo de expiração da conta acessada pelo cliente.
	 * Caso a conta já tenha sido expirada o cliente deverá ser informado sobre.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autenticação sobre o tempo de expiração da conta.
	 */

	private RefuseLogin authExpirationTime(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();

		if (!account.getExpiration().isNull() && account.getExpiration().get() < now())
		{
			logNotice("conta expirada (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return RL_EXPIRED;
		}

		return RL_OK;
	}

	/**
	 * Autentica o tempo de banimento da conta acessada pelo cliente.
	 * Caso a conta ainda esteja banida o cliente deverá ser informado sobre.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autenticação sobre o tempo de banimento da conta.
	 */

	private RefuseLogin authBanTime(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();

		if (!account.getUnban().isNull() && account.getUnban().get() < now())
		{
			logNotice("conta banida (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return RL_BANNED_UNTIL;
		}

		return RL_OK;
	}

	/**
	 * Autentica o estado atual da conta acessada pelo cliente.
	 * Caso a conta esteja em um estado inacessível o cliente deve ser informado sobre.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autenticação do estado atual da conta.
	 */

	private RefuseLogin authAccountState(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();

		if (account.getState() != NONE)
		{
			logNotice("conexão recusada (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return RefuseLogin.parse(account.getState().CODE - 1);
		}

		return RL_OK;
	}

	/**
	 * Autentica o hash passado pelo cliente para realizar o acesso com o servidor.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return resultado da autenticação do hash passado pelo cliente para com o servidor.
	 */

	private RefuseLogin authClientHash(LFileDescriptor fd, Account account, boolean server)
	{
		LoginSessionData sd = fd.getSessionData();

		if (getConfigs().getBool("client.hash_check") && !server)
		{
			if (sd.getClientHash() == null)
			{
				logNotice("client não enviou hash (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
				return RL_EXE_LASTED_VERSION;
			}

			Object object = getConfigs().getObject("client.hash_nodes");
			Node<ClientHash> node = null;
			boolean match = false;

			if (object != null)
				for (node = (ClientHashNode) object; node != null; node = node.getNext())
				{
					ClientHashNode chn = (ClientHashNode) node;

					if (account.getGroup().getCurrentGroup().getAccessLevel() < chn.getGroupLevel())
						continue;

					if (chn.get().getHashString().isEmpty() || chn.get().equals(sd.getClientHash()))
					{
						match = true;
						break;
					}
				}

			if (!match)
			{
				logNotice("client hash inválido (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
				return RL_EXE_LASTED_VERSION;
			}
		}

		return RL_OK;
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
	 * TODO
	 * @param ip
	 * @return
	 */

	public int parseSubnetIP(int ip)
	{
		// TODO lan_subnetcheck
		return 0;
	}

	/**
	 * TODO
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

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
	 * Controle para persist�ncia das contas de jogadores.
	 */
	private AccountControl accounts;

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
	 * Fun��o para remover informa��es de jogadores online que n�o est�o em um servidor de personagem v�lido.
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
	 * Procedimento chamado quando o servi�o de identifica��o do cliente tiver autenticado o mesmo.
	 * Aqui dever� ser autenticado os dados que foram passados pelo cliente em rela��o a uma conta.
	 * Dever� garantir primeiramente que o nome de usu�rio � v�lido para se fazer um acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return resultado da autentica��o da solicita��o para acesso de uma conta.
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

		logNotice("autentica��o aceita (id: %d, username: %s, ip: %s).\n", account.getID(), account.getUsername(), fd.getAddressString());

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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param server true se o cliente for um servidor ou false se for um jogador.
	 * @return resultado da obten��o dos dados da conta que o cliente passou,
	 * caso os dados tenham sido obtidos com �xito ficaram no cache do FileDescriptor.
	 */

	private RefuseLogin makeLoginAccount(LFileDescriptor fd, boolean server)
	{
		LoginSessionData sd = fd.getSessionData();
		Account account = accounts.get(sd.getUsername());

		if (account == null)
		{
			logNotice("usu�rio n�o encontrado (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
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
	 * Verifica primeiramente se est� habilitado a verifica��o para vers�o do cliente.
	 * Caso esteja habilitado a vers�o do cliente dever� ser igual a da configura��o definida.
	 * @param sd refer�ncia da sess�o que cont�m os dados de acesso do cliente em quest�o.
	 * @return resultado da autentica��o da vers�o que o cliente est� usando.
	 */

	private RefuseLogin authClientVersion(LoginSessionData sd)
	{
		if (getConfigs().getBool("client.check_version"))
		{
			int version = getConfigs().getInt("client.version");

			if (sd.getVersion() != version)
			{
				logNotice("vers�o inv�lida (account: %s, version (client/server): %d/%d).\n", sd.getUsername(), sd.getVersion(), version);
				return RL_EXE_LASTED_VERSION;
			}
		}

		return RL_OK;
	}

	/**
	 * Autentica se a senha passada pelo cliente corresponde com a senha da conta acessada.
	 * Caso n�o sejam iguais o cliente receber� uma mensagem de que a conta n�o pode ser acessada.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autentica��o da senha passada pelo cliente com a da conta.
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
	 * Autentica o tempo de expira��o da conta acessada pelo cliente.
	 * Caso a conta j� tenha sido expirada o cliente dever� ser informado sobre.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autentica��o sobre o tempo de expira��o da conta.
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
	 * Caso a conta ainda esteja banida o cliente dever� ser informado sobre.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autentica��o sobre o tempo de banimento da conta.
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
	 * Caso a conta esteja em um estado inacess�vel o cliente deve ser informado sobre.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autentica��o do estado atual da conta.
	 */

	private RefuseLogin authAccountState(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();

		if (account.getState() != NONE)
		{
			logNotice("conex�o recusada (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return RefuseLogin.parse(account.getState().CODE - 1);
		}

		return RL_OK;
	}

	/**
	 * Autentica o hash passado pelo cliente para realizar o acesso com o servidor.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return resultado da autentica��o do hash passado pelo cliente para com o servidor.
	 */

	private RefuseLogin authClientHash(LFileDescriptor fd, Account account, boolean server)
	{
		LoginSessionData sd = fd.getSessionData();

		if (getConfigs().getBool("client.hash_check") && !server)
		{
			if (sd.getClientHash() == null)
			{
				logNotice("client n�o enviou hash (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
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
				logNotice("client hash inv�lido (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
				return RL_EXE_LASTED_VERSION;
			}
		}

		return RL_OK;
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

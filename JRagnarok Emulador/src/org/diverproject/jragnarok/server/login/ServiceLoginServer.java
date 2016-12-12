package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.now;
import static org.diverproject.jragnarok.server.common.AuthResult.BANNED_UNTIL;
import static org.diverproject.jragnarok.server.common.AuthResult.EXE_LASTED_VERSION;
import static org.diverproject.jragnarok.server.common.AuthResult.EXPIRED;
import static org.diverproject.jragnarok.server.common.AuthResult.INCORRECT_PASSWORD;
import static org.diverproject.jragnarok.server.common.AuthResult.OK;
import static org.diverproject.jragnarok.server.common.AuthResult.UNREGISTERED_ID;
import static org.diverproject.jragnarok.server.login.entities.AccountState.NONE;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.common.AuthResult;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.OnlineMap;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.structures.ClientHash;
import org.diverproject.jragnarok.server.login.structures.ClientHashNode;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;
import org.diverproject.util.collection.Node;

/**
 * <h1>Servi�o de Acesso</h1>
 *
 * <p>Esse servi�o ir� receber uma solicita��o de conex�o do servi�o de acesso para clientes.
 * Essa solicita��o dever� ser autenticada conforme os dados da conta passados pelo cliente.
 * Os dados da conta ser�o obtidas dentro desse servi�o durante a autentica��o.</p>
 *
 * <p>Consistindo de autenticar o nome de usu�rio com a palavra chave (senha),
 * tempo de para expira��o da conta, tempo restante de banimento, vers�o do cliente e client hash.
 * Para a autentica��o de tempo de banimento ser� considerado independente de configura��es</p>
 *
 * @see AbstractServiceLogin
 * @see AccountControl
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
		onlines = getServer().getFacade().getOnlineControl();
	}

	@Override
	public void destroy()
	{
		accounts = null;
		onlines = null;
	}

	/**
	 * Fun��o para temporizadores executarem a remo��o de uma conta como acesso online.
	 */

	public final TimerListener WAITING_DISCONNECT_TIMER = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			onlines.remove(timer.getObjectID(), ServiceLoginServer.this.getTimerSystem().getTimers());
		}

		@Override
		public String getName()
		{
			return "waitingDisconnectTimer";
		}

		@Override
		public String toString()
		{
			return getName();
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

	public AuthResult parseAuthLogin(LFileDescriptor fd, boolean server)
	{
		AuthResult result = null;
		LoginSessionData sd = fd.getSessionData();

		if ((result = authClientVersion(sd)) != OK)
			return result;

		if ((result = makeLoginAccount(fd, server)) != OK)
			return result;

		Account account = (Account) sd.getCache();

		logNotice("autentica��o aceita (id: %d, username: %s, ip: %s).\n", account.getID(), account.getUsername(), fd.getAddressString());

		sd.setID(account.getID());
		sd.getLastLogin().set(account.getLastLogin().get());
		sd.setGroup(account.getGroup().getCurrentGroup());

		sd.getSeed().genFirst();
		sd.getSeed().genSecond();

		account.getLastLogin().set(now());
		account.getLastIP().set(fd.getAddress());
		account.setLoginCount(account.getLoginCount() + 1);

		if (!accounts.set(account))
			logError("falha ao persistir conta (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());

		return OK;
	}

	/**
	 * Comunica-se com o controle de contas para obter todos os dados da conta desejada.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param server true se o cliente for um servidor ou false se for um jogador.
	 * @return resultado da obten��o dos dados da conta que o cliente passou,
	 * caso os dados tenham sido obtidos com �xito ficaram no cache do FileDescriptor.
	 */

	private AuthResult makeLoginAccount(LFileDescriptor fd, boolean server)
	{
		LoginSessionData sd = fd.getSessionData();
		Account account = accounts.get(sd.getUsername());

		if (account == null)
		{
			logNotice("usu�rio n�o encontrado (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return UNREGISTERED_ID;
		}

		AuthResult result = AuthResult.OK;

		if ((result = authPassword(fd, account)) != OK)
			return result;

		if ((result = authExpirationTime(fd, account)) != OK)
			return result;

		if ((result = authBanTime(fd, account)) != OK)
			return result;

		if ((result = authAccountState(fd, account)) != OK)
			return result;

		if ((result = authClientHash(fd, account, server)) != OK)
			return result;

		sd.setCache(account);

		return OK;
	}

	/**
	 * Verifica primeiramente se est� habilitado a verifica��o para vers�o do cliente.
	 * Caso esteja habilitado a vers�o do cliente dever� ser igual a da configura��o definida.
	 * @param sd refer�ncia da sess�o que cont�m os dados de acesso do cliente em quest�o.
	 * @return resultado da autentica��o da vers�o que o cliente est� usando.
	 */

	private AuthResult authClientVersion(LoginSessionData sd)
	{
		if (getConfigs().getBool("client.check_version"))
		{
			int version = getConfigs().getInt("client.version");

			if (sd.getVersion() != version)
			{
				logNotice("vers�o inv�lida (account: %s, version (client/server): %d/%d).\n", sd.getUsername(), sd.getVersion(), version);
				return EXE_LASTED_VERSION;
			}
		}

		return OK;
	}

	/**
	 * Autentica se a senha passada pelo cliente corresponde com a senha da conta acessada.
	 * Caso n�o sejam iguais o cliente receber� uma mensagem de que a conta n�o pode ser acessada.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autentica��o da senha passada pelo cliente com a da conta.
	 */

	private AuthResult authPassword(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();
		String password = account.getPassword();

		if (!sd.getPassword().equals(password))
		{
			logNotice("senha incorreta (username: %s, password: %s, receive pass: %s, ip: %s).\n", sd.getUsername(), sd.getPassword(), password, fd.getAddressString());
			return INCORRECT_PASSWORD;
		}

		return OK;
	}

	/**
	 * Autentica o tempo de expira��o da conta acessada pelo cliente.
	 * Caso a conta j� tenha sido expirada o cliente dever� ser informado sobre.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autentica��o sobre o tempo de expira��o da conta.
	 */

	private AuthResult authExpirationTime(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();

		if (!account.getExpiration().isNull() && account.getExpiration().get() < now())
		{
			logNotice("conta expirada (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return EXPIRED;
		}

		return OK;
	}

	/**
	 * Autentica o tempo de banimento da conta acessada pelo cliente.
	 * Caso a conta ainda esteja banida o cliente dever� ser informado sobre.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autentica��o sobre o tempo de banimento da conta.
	 */

	private AuthResult authBanTime(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();

		if (!account.getUnban().isNull() && account.getUnban().get() < now())
		{
			logNotice("conta banida (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return BANNED_UNTIL;
		}

		return OK;
	}

	/**
	 * Autentica o estado atual da conta acessada pelo cliente.
	 * Caso a conta esteja em um estado inacess�vel o cliente deve ser informado sobre.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autentica��o do estado atual da conta.
	 */

	private AuthResult authAccountState(LFileDescriptor fd, Account account)
	{
		LoginSessionData sd = fd.getSessionData();

		if (account.getState() != NONE)
		{
			logNotice("conex�o recusada (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
			return AuthResult.parse(account.getState().CODE - 1);
		}

		return OK;
	}

	/**
	 * Autentica o hash passado pelo cliente para realizar o acesso com o servidor.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return resultado da autentica��o do hash passado pelo cliente para com o servidor.
	 */

	private AuthResult authClientHash(LFileDescriptor fd, Account account, boolean server)
	{
		LoginSessionData sd = fd.getSessionData();

		if (getConfigs().getBool("client.hash_check") && !server)
		{
			if (sd.getClientHash() == null)
			{
				logNotice("client n�o enviou hash (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
				return EXE_LASTED_VERSION;
			}

			Object object = getConfigs().getObject("client.hash_nodes");
			Node<ClientHash> node = null;
			boolean match = false;

			if (object != null)
				for (node = (ClientHashNode) object; node != null; node = node.getNext())
				{
					ClientHashNode chn = (ClientHashNode) node;

					if (account.getGroup().getID() < chn.getGroupID())
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
				return EXE_LASTED_VERSION;
			}
		}

		return OK;
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

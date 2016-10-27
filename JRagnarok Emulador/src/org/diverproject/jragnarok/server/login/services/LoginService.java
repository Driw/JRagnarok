package org.diverproject.jragnarok.server.login.services;

import static org.diverproject.jragnarok.JRagnarokUtil.now;
import static org.diverproject.jragnarok.server.login.entities.AccountState.NONE;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.BANNED_UNTIL;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.EXE_LASTED_VERSION;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.EXPIRED;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.INCORRECT_PASSWORD;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.OK;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.REJECTED_FROM_SERVER;
import static org.diverproject.jragnarok.server.login.structures.AuthResult.UNREGISTERED_ID;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.login.LoginServer;
import org.diverproject.jragnarok.server.login.controllers.AccountControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.Login;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.jragnarok.server.login.structures.ClientHash;
import org.diverproject.jragnarok.server.login.structures.ClientHashNode;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Node;

/**
 * <h1>Serviço de Acesso</h1>
 *
 * <p>Esse serviço irá receber uma solicitação de conexão do serviço de acesso para clientes.
 * Essa solicitação deverá ser autenticada conforme os dados da conta passados pelo cliente.
 * Os dados da conta serão obtidas dentro desse serviço durante a autenticação.</p>
 *
 * <p>Consistindo de autenticar o nome de usuário com a palavra chave (senha),
 * tempo de para expiração da conta, tempo restante de banimento, versão do cliente e client hash.
 * Para a autenticação de tempo de banimento será considerado independente de configurações</p>
 *
 * @see LoginServerService
 * @see AccountControl
 *
 * @author Andrew Mello
 */

public class LoginService extends LoginServerService
{
	/**
	 * Controle para intermediar a persistência de contas e cache de contas.
	 */
	private AccountControl controller;

	/**
	 * Constrói um novo serviço para realizar a autenticação e obtenção de contas.
	 * @param server referência do servidor de acesso do qual deseja criar o serviço.
	 * @throws RagnarokException caso não haja uma conexão válida estabelecida com o MySQL.
	 */

	public LoginService(LoginServer server) throws RagnarokException
	{
		super(server);

		controller = new AccountControl(getConnection());
	}

	/**
	 * Procedimento chamado quando o serviço de identificação do cliente tiver autenticado o mesmo.
	 * Aqui deverá ser autenticado os dados que foram passados pelo cliente em relação a uma conta.
	 * Deverá garantir primeiramente que o nome de usuário é válido para se fazer um acesso.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return resultado da autenticação da solicitação para acesso de uma conta.
	 */

	public AuthResult authLogin(LoginSessionData sd, boolean server)
	{
		AuthResult result = null;

		if ((result = authClientVersion(sd)) != OK)
			return result;

		if ((result = makeLoginAccount(sd, server)) != OK)
			return result;

		Account account = (Account) sd.getFileDescriptor().getCache();
		Login login = account.getLogin();

		logNotice("autenticação aceita (id: %d, username: %s, ip: %s).\n", login.getID(), login.getUsername(), sd.getAddressString());

		sd.setID(login.getID());
		sd.getLastLogin().set(login.getLastLogin().get());
		sd.setGroup(account.getGroup().getCurrentGroup());

		sd.getSeed().genFirst();
		sd.getSeed().genSecond();

		login.getLastLogin().set(now());
		account.getLastIP().set(sd.getAddress());
		account.setLoginCount(account.getLoginCount() + 1);

		try {

			controller.save(account);

		} catch (RagnarokException e) {
			logError("falha ao persistir conta (username: %s, ip: %s).\n", sd.getUsername(), sd.getAddressString());
			logExeception(e);
		}

		return OK;
	}

	/**
	 * Verifica primeiramente se está habilitado a verificação para versão do cliente.
	 * Caso esteja habilitado a versão do cliente deverá ser igual a da configuração definida.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @return resultado da autenticação da versão que o cliente está usando.
	 */

	private AuthResult authClientVersion(LoginSessionData sd)
	{
		if (getConfigs().getBool("client.check_version"))
		{
			int version = getConfigs().getInt("client.version");

			if (sd.getVersion() != version)
			{
				logNotice("versão inválida (account: %s, version (client/server): %d/%d).\n", sd.getUsername(), sd.getVersion(), version);
				return EXE_LASTED_VERSION;
			}
		}

		return OK;
	}

	/**
	 * Comunica-se com o controle de contas para obter todos os dados da conta desejada.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @param server true se o cliente for um servidor ou false se for um jogador.
	 * @return resultado da obtenção dos dados da conta que o cliente passou,
	 * caso os dados tenham sido obtidos com êxito ficaram no cache do FileDescriptor.
	 */

	private AuthResult makeLoginAccount(LoginSessionData sd, boolean server)
	{
		try {

			Account account = controller.get(sd.getUsername());

			if (account == null)
			{
				logNotice("usuário não encontrado (username: %s, ip: %s).\n", sd.getUsername(), sd.getAddressString());
				return UNREGISTERED_ID;
			}

			AuthResult result = AuthResult.OK;

			if ((result = authPassword(sd, account)) != OK)
				return result;

			if ((result = authExpirationTime(sd, account)) != OK)
				return result;

			if ((result = authBanTime(sd, account)) != OK)
				return result;

			if ((result = checkState(sd, account)) != OK)
				return result;

			if ((result = authClientHash(sd, account, server)) != OK)
				return result;

			sd.getFileDescriptor().setCache(account);

		} catch (RagnarokException e) {
			logExeception(e);
			return REJECTED_FROM_SERVER;
		}

		return OK;
	}

	/**
	 * Autentica se a senha passada pelo cliente corresponde com a senha da conta acessada.
	 * Caso não sejam iguais o cliente receberá uma mensagem de que a conta não pode ser acessada.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autenticação da senha passada pelo cliente com a da conta.
	 */

	private AuthResult authPassword(LoginSessionData sd, Account account)
	{
		String password = account.getLogin().getPassword();

		if (!sd.getPassword().equals(password))
		{
			logNotice("senha incorreta (username: %s, password: %s, receive pass: %s, ip: %s).\n", sd.getUsername(), sd.getPassword(), password, sd.getAddressString());
			return INCORRECT_PASSWORD;
		}

		return OK;
	}

	/**
	 * Autentica o tempo de expiração da conta acessada pelo cliente.
	 * Caso a conta já tenha sido expirada o cliente deverá ser informado sobre.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autenticação sobre o tempo de expiração da conta.
	 */

	private AuthResult authExpirationTime(LoginSessionData sd, Account account)
	{
		if (account.getExpiration().get() < now() && account.getExpiration().get() > 0)
		{
			logNotice("conta expirada (username: %s, ip: %s).\n", sd.getUsername(), sd.getAddressString());
			return EXPIRED;
		}

		return OK;
	}

	/**
	 * Autentica o tempo de banimento da conta acessada pelo cliente.
	 * Caso a conta ainda esteja banida o cliente deverá ser informado sobre.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autenticação sobre o tempo de banimento da conta.
	 */

	private AuthResult authBanTime(LoginSessionData sd, Account account)
	{
		if (account.getUnban().get() < now() && account.getUnban().get() > 0)
		{
			logNotice("conta banida (username: %s, ip: %s).\n", sd.getUsername(), sd.getAddressString());
			return BANNED_UNTIL;
		}

		return OK;
	}

	/**
	 * Autentica o estado atual da conta acessada pelo cliente.
	 * Caso a conta esteja em um estado inacessível o cliente deve ser informado sobre.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @return resultado da autenticação do estado atual da conta.
	 */

	private AuthResult checkState(LoginSessionData sd, Account account)
	{
		if (account.getState() != NONE)
		{
			logNotice("conexão recusada (username: %s, ip: %s).\n", sd.getUsername(), sd.getAddressString());
			return AuthResult.parse(account.getState().CODE - 1);
		}

		return OK;
	}

	/**
	 * Autentica o hash passado pelo cliente para realizar o acesso com o servidor.
	 * @param sd referência da sessão que contém os dados de acesso do cliente em questão.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return resultado da autenticação do hash passado pelo cliente para com o servidor.
	 */

	private AuthResult authClientHash(LoginSessionData sd, Account account, boolean server)
	{
		if (getConfigs().getBool("client.hash_check") && !server)
		{
			if (sd.getClientHash() == null)
			{
				logNotice("client não enviou hash (username: %s, ip: %s).\n", sd.getUsername(), sd.getAddressString());
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
				logNotice("client hash inválido (username: %s, ip: %s).\n", sd.getUsername(), sd.getAddressString());
				return EXE_LASTED_VERSION;
			}
		}

		return OK;
	}

	/**
	 * TODO ???
	 */

	public TimerListener waitingDisconnectTimer = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int tick)
		{
			
		}

		@Override
		public String getName()
		{
			return "waitingDisconnectTimer";
		}

		@Override
		public String toString()
		{
			ObjectDescription description = new ObjectDescription(getClass());

			description.append(getName());

			return description.toString();
		}
	};
}

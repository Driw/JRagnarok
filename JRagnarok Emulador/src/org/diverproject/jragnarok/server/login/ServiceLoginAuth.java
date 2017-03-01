package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnaork.JRagnarokConstants.DATE_FORMAT;
import static org.diverproject.jragnarok.JRagnarokUtil.binToHex;
import static org.diverproject.jragnarok.JRagnarokUtil.loginMessage;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Encrypt;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN2;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN3;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN4;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN_HAN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN_PCBANG;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_SSO_LOGIN_REQ;
import static org.diverproject.jragnarok.packets.common.NotifyAuth.NA_RECOGNIZES_LAST_LOGIN;
import static org.diverproject.jragnarok.packets.common.NotifyAuth.NA_SERVER_CLOSED;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_BANNED_UNTIL;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_EXE_LASTED_VERSION;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_EXPIRED;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_INCORRECT_PASSWORD;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_OK;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_REJECTED_FROM_SERVER;
import static org.diverproject.jragnarok.packets.common.RefuseLogin.RL_UNREGISTERED_ID;
import static org.diverproject.jragnarok.server.ServerState.RUNNING;
import static org.diverproject.jragnarok.server.login.entities.AccountState.NONE;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.b;
import static org.diverproject.util.Util.format;
import static org.diverproject.util.Util.now;

import org.diverproject.jragnarok.packets.common.RefuseLogin;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_CharServerConnect;
import org.diverproject.jragnarok.packets.login.fromclient.CA_Login;
import org.diverproject.jragnarok.packets.login.fromclient.CA_Login2;
import org.diverproject.jragnarok.packets.login.fromclient.CA_Login3;
import org.diverproject.jragnarok.packets.login.fromclient.CA_Login4;
import org.diverproject.jragnarok.packets.login.fromclient.CA_LoginHan;
import org.diverproject.jragnarok.packets.login.fromclient.CA_LoginPCBang;
import org.diverproject.jragnarok.packets.login.fromclient.CA_LoginSingleSignOn;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.ServerState;
import org.diverproject.jragnarok.server.common.CharServerType;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.SocketUtil;
import org.diverproject.util.Time;
import org.diverproject.util.collection.Node;
import org.diverproject.util.lang.IntUtil;

/**
 * <h1>Servi�o para Autenticar Acesso</h1>
 *
 * <p>Esse servi�o dever� ser respons�vel pela autentica��o dos dados recebidos de um cliente.
 * Far� o contato com o banco de dados solicitando os dados para realizar a devida autentica��o.
 * Caso haja problemas durante a autentica��o dever� repassar ao cliente os problemas encontrados.
 * Tamb�m fica de sua conta reconhecer o tipo de acesso que o cliente est� solicitando.</p>
 *
 * @see AbstractServiceLogin
 * @see ServiceLoginServer
 * @see ServiceLoginChar
 * @see ServiceLoginClient
 * @see ServiceLoginIpBan
 * @see ServiceLoginLog
 * @see OnlineMap
 * @see AuthAccountMap
 *
 * @author Andrew
 */

public class ServiceLoginAuth extends AbstractServiceLogin
{
	/**
	 * Servi�o para comunica��o entre o servidor e o cliente.
	 */
	private ServiceLoginClient client;

	/**
	 * Controle para persist�ncia das contas de jogadores.
	 */
	private AccountControl accounts;

	/**
	 * Servi�o para acesso de contas (servi�o principal)
	 */
	private ServiceLoginServer login;

	/**
	 * Servi�o para banimento de acessos por endere�o de IP.
	 */
	private ServiceLoginIpBan ipban;

	/**
	 * Servi�o para registro de acessos.
	 */
	private ServiceLoginLog log;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthAccountMap auths;

	/**
	 * Cria um novo servi�o para autentica��o de solicita��es dos acessos ao servidor.
	 * Esse servi�o far� o contato com o banco de dados e autentica��es necess�rias.
	 * @param server refer�ncia do servidor de acesso que deseja criar o servi�o.
	 */

	public ServiceLoginAuth(LoginServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getClientService();
		accounts = getServer().getFacade().getAccountControl();
		ipban = getServer().getFacade().getIpBanService();
		log = getServer().getFacade().getLogService();
		login = getServer().getFacade().getLoginService();
		auths = getServer().getFacade().getAuthAccountMap();
	}

	@Override
	public void destroy()
	{
		client = null;
		accounts = null;
		ipban = null;
		log = null;
		login = null;
		auths = null;
	}

	/**
	 * Efetua a solicita��o de acesso com o servidor de personagens recebido de um cliente.
	 * Para este caso o cliente j� � reconhecido como um jogador atrav�s do execut�vel.
	 * Dever� receber os dados do cliente adequadamente conforme o tipo de autentica��o.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param command qual o comando que foi executado (tipo de pacote).
	 * @return true se efetuar a an�lise com �xito ou false caso contr�rio.
	 */

	public boolean parseClient(LFileDescriptor fd, short command)
	{
		boolean usingRawPassword = true;
		LoginSessionData sd = fd.getSessionData();

		switch (command)
		{
			case PACKET_CA_LOGIN:
				CA_Login loginPacket = new CA_Login();
				loginPacket.receive(fd);
				sd.setVersion(loginPacket.getVersion());
				sd.setClientType(loginPacket.getClientType());
				sd.setUsername(loginPacket.getUsername());
				sd.setPassword(loginPacket.getPassword());
				break;

			case PACKET_CA_LOGIN_PCBANG:
				CA_LoginPCBang loginPCBang = new CA_LoginPCBang();
				loginPCBang.receive(fd);
				sd.setVersion(loginPCBang.getVersion());
				sd.setClientType(loginPCBang.getClientType());
				sd.setUsername(loginPCBang.getUsername());
				sd.setPassword(loginPCBang.getPassword());
				break;

			case PACKET_CA_LOGIN_HAN:
				CA_LoginHan loginHan = new CA_LoginHan();
				loginHan.receive(fd);
				sd.setVersion(loginHan.getVersion());
				sd.setClientType(loginHan.getClientType());
				sd.setUsername(loginHan.getUsername());
				sd.setPassword(loginHan.getPassword());
				break;

			case PACKET_CA_SSO_LOGIN_REQ:
				CA_LoginSingleSignOn loginSingleSignOn = new CA_LoginSingleSignOn();
				loginSingleSignOn.receive(fd);
				sd.setVersion(loginSingleSignOn.getVersion());
				sd.setClientType(loginSingleSignOn.getClientType());
				sd.setUsername(loginSingleSignOn.getUsername());
				sd.setPassword(loginSingleSignOn.getToken());
				break;

			case PACKET_CA_LOGIN2:
				CA_Login2 loginMD5 = new CA_Login2();
				loginMD5.receive(fd);
				sd.setVersion(loginMD5.getVersion());
				sd.setUsername(loginMD5.getUsername());
				sd.setPassword(loginMD5.getPassword());
				sd.setClientType(loginMD5.getClientType());
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN3:
				CA_Login3 loginMD5Info = new CA_Login3();
				loginMD5Info.receive(fd);
				sd.setVersion(loginMD5Info.getVersion());
				sd.setUsername(loginMD5Info.getUsername());
				sd.setPassword(loginMD5Info.getPassword());
				sd.setClientType(loginMD5Info.getClientType());
				usingRawPassword = false;
				break;

			case PACKET_CA_LOGIN4:
				CA_Login4 loginMD5Mac = new CA_Login4();
				loginMD5Mac.receive(fd);
				sd.setVersion(loginMD5Mac.getVersion());
				sd.setUsername(loginMD5Mac.getUsername());
				sd.setPassword(loginMD5Mac.getPassword());
				sd.setClientType(loginMD5Mac.getClientType());
				usingRawPassword = false;
				break;
		}

		if (!parsePassword(fd, usingRawPassword))
			return false;

		return parseAuthentication(fd, false);
	}

	/**
	 * Procedimento que ir� fazer a conclus�o da autentica��o da solicita��o de um cliente.
	 * Neste momentos os dados passados pelo cliente j� ter�o sido lidos e guardados na sess�o.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param usingRawPassword true se estiver usando senha direta ou false se for md5.
	 * @return true se for autenticado com �xito ou false caso contr�rio.
	 */

	private boolean parsePassword(LFileDescriptor fd, boolean usingRawPassword)
	{
		LoginSessionData sd = fd.getSessionData();

		if (usingRawPassword)
		{
			logNotice("solicita��o de conex�o de %s (ip: %s, version: %d)\n", sd.getUsername(), fd.getAddressString(), sd.getVersion());

			if (config().useMD5Password)
				sd.setPassword(md5Encrypt(sd.getPassword()));

			sd.getPassDencrypt().setValue(b(0));
		}

		else
		{
			log("solicita��o de conex�o passdenc de %s (ip: %s, version: %d)\n", sd.getUsername(), fd.getAddressString(), sd.getVersion());

			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT);
			sd.getPassDencrypt().set(LoginSessionData.PASSWORD_DENCRYPT2);
			sd.setPassword(binToHex(sd.getPassword(), 16));
		}

		if (sd.getPassDencrypt().getValue() != 0 && config().useMD5Password)
		{
			client.refuseLogin(fd, RL_REJECTED_FROM_SERVER);
			return false;
		}

		return true;
	}

	/**
	 * Procedimento chamado quando o servi�o de identifica��o do cliente tiver autenticado o mesmo.
	 * Aqui dever� ser autenticado os dados que foram passados pelo cliente em rela��o a uma conta.
	 * Dever� garantir primeiramente que o nome de usu�rio � v�lido para se fazer um acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return true para manter a conex�o ou false se for para fechar a conex�o.
	 */

	public boolean parseAuthentication(LFileDescriptor fd, boolean server)
	{
		RefuseLogin result = null;
		LoginSessionData sd = fd.getSessionData();

		if (((result = authClientVersion(sd)) != RL_OK) ||
			((result = makeLoginAccount(fd, server)) != RL_OK))
		{
			authFailed(fd, result);
			return false;
		}

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
			logError("falha ao atualizar acesso (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());

		// Contas de servidores n�o precisam ser registrados como online
		if (server)
			return true;

		return authOk(fd);
	}

	/**
	 * Comunica-se com o controle de contas para obter todos os dados da conta desejada.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
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

		if (((result = authPassword(fd, account)) != RL_OK) ||
			((result = authExpirationTime(fd, account)) != RL_OK) ||
			((result = authBanTime(fd, account)) != RL_OK) ||
			((result = authAccountState(fd, account)) != RL_OK) ||
			((result = authClientHash(fd, account, server)) != RL_OK))
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
		if (config().checkVersion)
		{
			int version = config().version;

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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param account objeto contendo os dados da conta do qual o cliente tentou acessar.
	 * @param server true se o cliente for um servidor ou false caso seja um jogador.
	 * @return resultado da autentica��o do hash passado pelo cliente para com o servidor.
	 */

	private RefuseLogin authClientHash(LFileDescriptor fd, Account account, boolean server)
	{
		LoginSessionData sd = fd.getSessionData();

		if (config().hashCheck && !server)
		{
			if (sd.getClientHash() == null)
			{
				logNotice("client n�o enviou hash (username: %s, ip: %s).\n", sd.getUsername(), fd.getAddressString());
				return RL_EXE_LASTED_VERSION;
			}

			boolean match = false;
			Node<ClientHash> node = config().hashNodes;

			while (node != null && node.get() != null)
			{
				ClientHashNode chn = (ClientHashNode) node;
	
				if (account.getGroup().getCurrentGroup().getAccessLevel() < chn.getGroupLevel())
					continue;
	
				if (chn.get().getHashString().isEmpty() || chn.get().equals(sd.getClientHash()))
				{
					match = true;
					break;
				}

				node = node.getNext();
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
	 * Chamado internamente sempre que uma solicita��o de acesso tiver falhado na autentica��o.
	 * Deve registrar a falha se habilitado o log e responder ao cliente qual o motivo da falha.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailed(LFileDescriptor fd, RefuseLogin result)
	{
		authFailedLog(fd, result);
		authFailedResponse(fd, result);
	}

	/**
	 * Registra uma solicita��o de acesso que n�o foi autenticada corretamente.
	 * Esse registro � feito no banco de dados para identificar quem falhou.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailedLog(LFileDescriptor fd, RefuseLogin result)
	{
		LoginSessionData sd = fd.getSessionData();

		if (config().logLogin)
		{
			if (IntUtil.interval(result.CODE, 0, 15))
				log.add(fd.getAddress(), sd, result.CODE, loginMessage(result.CODE));

			else if (IntUtil.interval(result.CODE, 99, 104))
				log.add(fd.getAddress(), sd, result.CODE, loginMessage(result.CODE-83));

			else
				log.add(fd.getAddress(), sd, result.CODE, loginMessage(22));
		}		

		if (result.CODE == 0 || result.CODE == 1)
			ipban.addBanLog(fd.getAddressString());
	}

	/**
	 * Procedimento que ir� responder ao cliente os detalhes do resultado da autentica��o.
	 * Caso o cliente esteja banido no servidor ir� informar at� quando o mesmo ocorre.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param result resultando obtido da autentica��o feita com o cliente.
	 */

	private void authFailedResponse(LFileDescriptor fd, RefuseLogin result)
	{
		String blockDate = "";
		LoginSessionData sd = fd.getSessionData();

		if (result == RL_BANNED_UNTIL)
		{
			Account account = (Account) sd.getCache();
			Time unbanTime = account.getUnban();

			if (unbanTime != null && unbanTime.get() > 0)
				blockDate = unbanTime.toStringFormat(DATE_FORMAT);
			else
				blockDate = "! Banido !";
		}

		client.refuseLogin(fd, result, blockDate);
	}

	/**
	 * Chamado internamente sempre que uma solicita��o de acesso tiver sido aprovada na autentica��o.
	 * O segundo passo � verificar a conex�o e estado do servidor, grupos habilitados e se est� online.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @return true se conseguir completar a autentica��o com sucesso ou false caso contr�rio.
	 */

	private boolean authOk(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();

		if (!authServerConnected(fd) || !authServerState(sd) || !authGroupAccount(fd))
		{
			client.notifyBan(fd, NA_SERVER_CLOSED);
			return false;
		}

		if (!login.isOnline(fd))
		{
			auths.remove(sd.getID());
			client.notifyBan(fd, NA_RECOGNIZES_LAST_LOGIN);

			return false;
		}

		Account account = (Account) sd.getCache();

		log.add(new InternetProtocol(fd.getAddress()), account, 100, "login ok");

		logNotice("conex�o da conta '%s' aceita.\n", sd.getUsername());

		AuthNode node = new AuthNode();
		node.setAccountID(sd.getID());
		node.getSeed().copyFrom(sd.getSeed());
		node.getIP().set(fd.getAddress());
		node.setVersion(sd.getVersion());
		node.setClientType(sd.getClientType());
		node.setSex(sd.getSex());
		auths.add(node);

		login.addOnlineUser(OnlineLogin.NO_CHAR_SERVER, sd.getID());
		client.sendCharServerList(fd);

		return true;
	}

	/**
	 * Faz a autentica��o para verificar se h� algum servidor de personagens conectado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @return true se houver ao menos um servidor ou false se n�o houver nenhum
	 */

	private boolean authServerConnected(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();
		Account account = (Account) sd.getCache();
		String username = account.getUsername();

		int serverConnect = 0;
		CharServerList servers = getServer().getCharServerList();

		for (ClientCharServer server : servers)
			if (server.getFileDecriptor().isConnected())
				serverConnect++;

		if (serverConnect == 0)
			logNotice("nenhum servidor de personagens conectado (usernme: %s).\n", username);

		return serverConnect > 0;
	}

	/**
	 * Faz a autentica��o do estado do servidor com o qual est� tentando se conectar.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se estiver rodando ou false caso contr�rio.
	 */

	private boolean authServerState(LoginSessionData sd)
	{
		boolean ok = getServer().getState() == RUNNING;

		if (!ok)
			logNotice("servidor n�o est� rodando (state: %s, usernme: %s).\n", getServer().getState(), sd.getUsername());

		return ok;
	}

	/**
	 * Faz a autentica��o para verificar se o cliente est� contido nos grupos habilitados.
	 * Essa autentica��o s� ser� v�lida caso tenha sido configurado grupos de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @return true se estiver habilitado a conectar-se nesse servidor.
	 */

	private boolean authGroupAccount(LFileDescriptor fd)
	{
		int groupToConnect = config().groupToConnect;
		int minGroupToConnect = config().minGroupToConnect;

		if (groupToConnect == 0 && minGroupToConnect == 0)
			return true;

		Account account = (Account) fd.getSessionData().getCache();

		return	authGroupToConnect(account, groupToConnect) ||
				authMinGroupToConnect(account, groupToConnect, minGroupToConnect);
	}

	/**
	 * Autentica uma conta em rela��o ao �nico grupo de contas habilitado a conectar.
	 * @param account refer�ncia da conta do cliente que dever� ser autenticada.
	 * @param required n�vel da conta necess�ria para se contar.
	 * @return true se estiver habilitado ou false caso contr�rio.
	 */

	private boolean authGroupToConnect(Account account, int required)
	{
		int group = account.getGroup().getCurrentGroup().getID();
		String username = account.getUsername();

		if (required == 0 || group == required)
			return true;

		logNotice("grupo '%d' � o �nico aceito (username: %s, grupoid: %d).\n", required, username, group);

		return false;
	}

	/**
	 * Autentica uma conta para ver se possui n�vel de grupo maior do m�nimo aceito.
	 * Essa verifica��o s� ser� v�lida caso o grupo requerido seja igual a -1.
	 * @param account refer�ncia da conta do cliente que dever� ser autenticada.
	 * @param required n�vel do grupo para se conectar ao servidor (esperado -1).
	 * @param min n�vel do grupo m�nimo aceito para se conectar no servidor.
	 * @return true se tiver um grupo habilitado ou false caso contr�rio.
	 */

	private boolean authMinGroupToConnect(Account account, int required, int min)
	{
		int group = account.getGroup().getCurrentGroup().getID();
		String username = account.getUsername();

		if (required == -1 && min < group)
			return true;

		logNotice("grupo '%d' � o m�nimo aceito (usernme: %s, groupid: %d).\n", min, username, group);

		return false;
	}

	/**
	 * Chamado quando um servidor de personagens solicita a conex�o com o servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de acesso.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 * @return true se tiver sido autorizado ou false caso contr�rio.
	 */

	public boolean parseCharServer(LFileDescriptor fd)
	{
		HA_CharServerConnect packet = new HA_CharServerConnect();
		packet.receive(fd);

		Account account = accounts.get(packet.getUsername());

		if (account == null)
		{
			logWarning("char-server com usu�rio n�o encontrado (name: %s, username: %s).\n", packet.getServerName(), packet.getUsername());
			return false;
		}

		if (!account.getPassword().equals(packet.getPassword()))
		{
			logWarning("char-server com senha incompat�vel (name: %s, username: %s).\n", packet.getServerName(), packet.getUsername());
			return false;
		}
		
		LoginSessionData sd = fd.getSessionData();
		sd.setID(account.getID());
		sd.setUsername(packet.getUsername());
		sd.setPassword(packet.getPassword());
		sd.setGroup(account.getGroup().getCurrentGroup());

		if (config().useMD5Password)
			sd.setPassword(md5Encrypt(sd.getPassword()));

		sd.getPassDencrypt().setValue(b(0));
		sd.setVersion(config().version);

		String serverName = packet.getServerName();
		int serverIP = packet.getServerIP();
		short serverPort = packet.getServerPort();
		short type = packet.getType();
		boolean newDisplay = packet.getNewDisplay();

		logInfo("conex�o solicitada do servidor de personagens %s@%s (account: %s, pass: %s).\n", serverName, fd.getAddressString(), sd.getUsername(), sd.getPassword());

		String message = format("charserver - %s@%s:%d", serverName, SocketUtil.socketIP(serverIP), serverPort);
		log.add(fd.getAddress(), sd, 100, message);

		if (parseAuthentication(fd, true) && getServer().isState(ServerState.RUNNING) && fd.isConnected())
		{
			logNotice("conex�o do servidor de personagens '%s' aceita.\n", serverName);

			ClientCharServer server = new ClientCharServer();
			server.setFileDecriptor(fd);
			server.setName(serverName);
			server.getIP().set(serverIP);
			server.setPort(serverPort);
			server.setUsers((short) 0);
			server.setType(CharServerType.parse(type));
			server.setNewDisplay(newDisplay);
			getServer().getCharServerList().add(server);

			fd.setParseListener(getServer().getFacade().PARSE_CHAR_SERVER);
			fd.getFlag().set(FileDescriptor.FLAG_SERVER);

			client.sendCharServerResult(fd, RL_OK);
			client.sendGroupData(fd, getServer().getFacade().getGroupControl());

			return true;
		}

		logNotice("Conex�o com o servidor de personagens '%s' RECUSADA.\n", serverName);

		client.sendCharServerResult(fd, RL_REJECTED_FROM_SERVER);
		return false;
	}
}

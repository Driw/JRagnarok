package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnarok.server.ServerService;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.AuthControl;
import org.diverproject.jragnarok.server.login.control.GroupControl;
import org.diverproject.jragnarok.server.login.control.IpBanControl;
import org.diverproject.jragnarok.server.login.control.LoginLogControl;
import org.diverproject.jragnarok.server.login.control.OnlineControl;
import org.diverproject.jragnarok.server.login.control.PincodeControl;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Servi�o Abstrato para Servidor de Acesso</h1>
 *
 * <p>O servidor abstrato tem apenas como objetivo facilitar a codifica��o dos servi�os.
 * Conhece todas as refer�ncias de servi�os e controles referentes ao servidor de acesso.
 * Para tal, � necess�rio que todos os servi�os sejam inicializados e destru�dos.</p>
 *
 * @see ServiceLoginAccount
 * @see ServiceLoginAuth
 * @see ServiceLoginChar
 * @see ServiceLoginClient
 * @see ServiceLoginIpBan
 * @see ServiceLoginLog
 * @see ServiceLoginServer
 * @see AccountControl
 * @see AuthControl
 * @see GroupControl
 * @see IpBanControl
 * @see LoginLogControl
 * @see OnlineControl
 * @see PincodeControl
 *
 * @author Andrew
 */

class AbstractServiceLogin extends ServerService
{
	/**
	 * Servi�o para comunica��o com o servidor de personagens.
	 */
	protected ServiceLoginChar loginchar;

	/**
	 * Servi�o para comunica��o entre o servidor e o cliente.
	 */
	protected ServiceLoginClient client;

	/**
	 * Servi�o para banimento de acessos por endere�o de IP.
	 */
	protected ServiceLoginIpBan ipban;

	/**
	 * Servi�o para registro de acessos.
	 */
	protected ServiceLoginLog log;

	/**
	 * Servi�o para acesso de contas (servi�o principal)
	 */
	protected ServiceLoginServer login;

	/**
	 * Servi�o para autentica��o de acessos.
	 */
	protected ServiceLoginAuth auth;

	/**
	 * Servi�o para trabalhar com as contas dos jogadores.
	 */
	protected ServiceLoginAccount account;

	/**
	 * Controle para persist�ncia das contas de jogadores.
	 */
	protected AccountControl accounts;

	/**
	 * Controle para persist�ncia e cache dos grupos de jogadores.
	 */
	protected GroupControl groups;

	/**
	 * Controle para persist�ncia dos c�digo PIN das contas de jogadores.
	 */
	protected PincodeControl pincodes;

	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	protected LoginLogControl logs;

	/**
	 * Controle para banimento de endere�os de IP.
	 */
	protected IpBanControl ipbans;

	/**
	 * Controlador para identificar jogadores online.
	 */
	protected OnlineControl onlines;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	protected AuthControl auths;

	/**
	 * Instancia um novo servi�o abstrato que permite ir� permitir a comunica��o entre servi�os.
	 * Todos os servi�os e controles podem ser solicitados internamente por esta classe.
	 * @param server refer�ncia do servidor de acesso que det�m este servi�o.
	 */

	public AbstractServiceLogin(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa os servi�os e controles do servidor de acesso definido as suas refer�ncias.
	 * As refer�ncias s�o obtidas atrav�s do servidor de acesso que det�m o servi�o.
	 */

	public void init()
	{
		loginchar = getServer().getFacade().getCharService();
		client = getServer().getFacade().getClientService();
		ipban = getServer().getFacade().getIpBanService();
		log = getServer().getFacade().getLogService();
		login = getServer().getFacade().getLoginService();
		auth = getServer().getFacade().getAuthService();
		account = getServer().getFacade().getAccountService();

		accounts = getServer().getFacade().getAccountControl();
		groups = getServer().getFacade().getGroupControl();
		pincodes = getServer().getFacade().getPincodeControl();
		logs = getServer().getFacade().getLoginLogControl();
		ipbans = getServer().getFacade().getIpBanControl();
		onlines = getServer().getFacade().getOnlineControl();
		auths = getServer().getFacade().getAuthControl();
	}

	/**
	 * Remover todas as refer�ncias de controles e servi�os para que o servidor possa destru�-los.
	 * Caso os servi�os n�o sejam removidos das refer�ncias podem ocupar mem�ria desnecess�ria.
	 */

	public void destroy()
	{
		loginchar = null;
		client = null;
		ipban = null;
		log = null;
		login = null;
		auth = null;
		account = null;

		accounts = null;
		groups = null;
		pincodes = null;
		logs = null;
		ipbans = null;
		onlines = null;
		auths = null;
	}

	@Override
	protected LoginServer getServer()
	{
		return (LoginServer) super.getServer();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("state", getServer().getState());

		return description.toString();
	}
}

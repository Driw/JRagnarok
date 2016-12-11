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
 * <h1>Serviço Abstrato para Servidor de Acesso</h1>
 *
 * <p>O servidor abstrato tem apenas como objetivo facilitar a codificação dos serviços.
 * Conhece todas as referências de serviços e controles referentes ao servidor de acesso.
 * Para tal, é necessário que todos os serviços sejam inicializados e destruídos.</p>
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
	 * Serviço para comunicação com o servidor de personagens.
	 */
	protected ServiceLoginChar loginchar;

	/**
	 * Serviço para comunicação entre o servidor e o cliente.
	 */
	protected ServiceLoginClient client;

	/**
	 * Serviço para banimento de acessos por endereço de IP.
	 */
	protected ServiceLoginIpBan ipban;

	/**
	 * Serviço para registro de acessos.
	 */
	protected ServiceLoginLog log;

	/**
	 * Serviço para acesso de contas (serviço principal)
	 */
	protected ServiceLoginServer login;

	/**
	 * Serviço para autenticação de acessos.
	 */
	protected ServiceLoginAuth auth;

	/**
	 * Serviço para trabalhar com as contas dos jogadores.
	 */
	protected ServiceLoginAccount account;

	/**
	 * Controle para persistência das contas de jogadores.
	 */
	protected AccountControl accounts;

	/**
	 * Controle para persistência e cache dos grupos de jogadores.
	 */
	protected GroupControl groups;

	/**
	 * Controle para persistência dos código PIN das contas de jogadores.
	 */
	protected PincodeControl pincodes;

	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	protected LoginLogControl logs;

	/**
	 * Controle para banimento de endereços de IP.
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
	 * Instancia um novo serviço abstrato que permite irá permitir a comunicação entre serviços.
	 * Todos os serviços e controles podem ser solicitados internamente por esta classe.
	 * @param server referência do servidor de acesso que detém este serviço.
	 */

	public AbstractServiceLogin(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa os serviços e controles do servidor de acesso definido as suas referências.
	 * As referências são obtidas através do servidor de acesso que detém o serviço.
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
	 * Remover todas as referências de controles e serviços para que o servidor possa destruí-los.
	 * Caso os serviços não sejam removidos das referências podem ocupar memória desnecessária.
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

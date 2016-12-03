package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnarok.server.ServerService;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.AuthControl;
import org.diverproject.jragnarok.server.login.control.GroupControl;
import org.diverproject.jragnarok.server.login.control.IpBanControl;
import org.diverproject.jragnarok.server.login.control.LoginLogControl;
import org.diverproject.jragnarok.server.login.control.OnlineControl;
import org.diverproject.jragnarok.server.login.control.PincodeControl;

/**
 * Servi�o Abstrato para Servidor de Acesso
 *
 * Este servi�o ir� 
 *
 * @author Andrew
 */

public class AbstractServiceLogin extends ServerService
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
	protected AccountControl accountControl;

	/**
	 * Controle para persist�ncia e cache dos grupos de jogadores.
	 */
	protected GroupControl groupControl;

	/**
	 * Controle para persist�ncia dos c�digo PIN das contas de jogadores.
	 */
	protected PincodeControl pincodeControl;

	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	protected LoginLogControl logControl;

	/**
	 * Controle para banimento de endere�os de IP.
	 */
	protected IpBanControl ipbanControl;

	/**
	 * Controlador para identificar jogadores online.
	 */
	protected OnlineControl onlineControl;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	protected AuthControl authControl;

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
		loginchar = getServer().getCharService();
		client = getServer().getClientService();
		ipban = getServer().getIpBanService();
		log = getServer().getLogService();
		login = getServer().getLoginService();
		auth = getServer().getAuthService();
		account = getServer().getAccountService();

		accountControl = getServer().getAccountControl();
		groupControl = getServer().getGroupControl();
		pincodeControl = getServer().getPincodeControl();
		logControl = getServer().getLoginLogControl();
		ipbanControl = getServer().getIpBanControl();
		onlineControl = getServer().getOnlineControl();
		authControl = getServer().getAuthControl();
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

		accountControl = null;
		groupControl = null;
		pincodeControl = null;
		logControl = null;
		ipbanControl = null;
		onlineControl = null;
		authControl = null;
	}

	@Override
	protected LoginServer getServer()
	{
		return (LoginServer) super.getServer();
	}
}

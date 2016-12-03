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
 * Serviço Abstrato para Servidor de Acesso
 *
 * Este serviço irá 
 *
 * @author Andrew
 */

public class AbstractServiceLogin extends ServerService
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
	protected AccountControl accountControl;

	/**
	 * Controle para persistência e cache dos grupos de jogadores.
	 */
	protected GroupControl groupControl;

	/**
	 * Controle para persistência dos código PIN das contas de jogadores.
	 */
	protected PincodeControl pincodeControl;

	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	protected LoginLogControl logControl;

	/**
	 * Controle para banimento de endereços de IP.
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

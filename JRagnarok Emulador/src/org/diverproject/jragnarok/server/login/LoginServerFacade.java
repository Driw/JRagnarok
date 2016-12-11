package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOG_LOGIN;

import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.AuthControl;
import org.diverproject.jragnarok.server.login.control.GroupControl;
import org.diverproject.jragnarok.server.login.control.IpBanControl;
import org.diverproject.jragnarok.server.login.control.LoginLogControl;
import org.diverproject.jragnarok.server.login.control.OnlineControl;
import org.diverproject.jragnarok.server.login.control.PincodeControl;

/**
 * <h1>Servidor de Acesso - Fa�ade</h1>
 *
 * <p>Essa classe � usada para centralizar todos os servi�os e controles do servidor de acesso.
 * Atrav�s dele os servi�os poder�o comunicar-se entre si como tamb�m chamar os controles dispon�veis.
 * Possui m�todos que ir�o realizar a cria��o das inst�ncias e destrui��o das mesmas quando necess�rio.</p>
 *
 * @see CharServerList
 * @see ServiceLoginChar
 * @see ServiceLoginClient
 * @see ServiceLoginIpBan
 * @see ServiceLoginLog
 * @see ServiceLoginServer
 *
 * @author Andrew
 */

class LoginServerFacade
{
	/**
	 * Servi�o para comunica��o com o servidor de personagens.
	 */
	private ServiceLoginChar charService;

	/**
	 * Servi�o para comunica��o entre o servidor e o cliente.
	 */
	private ServiceLoginClient clientService;

	/**
	 * Servi�o para banimento de acessos por endere�o de IP.
	 */
	private ServiceLoginIpBan ipBanService;

	/**
	 * Servi�o para registro de acessos.
	 */
	private ServiceLoginLog logService;

	/**
	 * Servi�o para acesso de contas (servi�o principal)
	 */
	private ServiceLoginServer loginService;

	/**
	 * Servi�o para autentica��o de acessos.
	 */
	private ServiceLoginAuth authService;

	/**
	 * Servi�o para trabalhar com as contas dos jogadores.
	 */
	private ServiceLoginAccount accountService;


	/**
	 * Controle para persist�ncia das contas de jogadores.
	 */
	private AccountControl accountControl;

	/**
	 * Controle para persist�ncia e cache dos grupos de jogadores.
	 */
	private GroupControl groupControl;

	/**
	 * Controle para persist�ncia dos c�digo PIN das contas de jogadores.
	 */
	private PincodeControl pincodeControl;

	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	private LoginLogControl logControl;

	/**
	 * Controle para banimento de endere�os de IP.
	 */
	private IpBanControl ipbanControl;

	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineControl onlineControl;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthControl authControl;

	/**
	 * @return aquisi��o do servi�o para comunica��o com o servidor de personagens.
	 */

	public ServiceLoginChar getCharService()
	{
		return charService;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o do servidor com o cliente.
	 */

	public ServiceLoginClient getClientService()
	{
		return clientService;
	}

	/**
	 * @return aquisi��o do servi�o para banimento de acessos por endere�o de IP.
	 */

	public ServiceLoginIpBan getIpBanService()
	{
		return ipBanService;
	}

	/**
	 * @return aquisi��o do servi�o para registro de acessos no servidor.
	 */

	public ServiceLoginLog getLogService()
	{
		return logService;
	}

	/**
	 * @return aquisi��o do servi�o para acesso de contas (servi�o principal)
	 */

	public ServiceLoginServer getLoginService()
	{
		return loginService;
	}

	/**
	 * @return aquisi��o do servi�o para autentica��o de acessos.
	 */

	public ServiceLoginAuth getAuthService()
	{
		return authService;
	}

	/**
	 * @return aquisi��o do servi�o para trabalhar com as contas dos jogadores.
	 */

	public ServiceLoginAccount getAccountService()
	{
		return accountService;
	}

	/**
	 * @return aquisi��o do controle para gerenciar as contas dos jogadores.
	 */

	public AccountControl getAccountControl()
	{
		return accountControl;
	}

	/**
	 * @return aquisi��o do controle para gerenciar os grupos de jogadores.
	 */

	public GroupControl getGroupControl()
	{
		return groupControl;
	}

	/**
	 * @return aquisi��o do controle para gerenciar os c�digos PIN de contas.
	 */

	public PincodeControl getPincodeControl()
	{
		return pincodeControl;
	}

	/**
	 * @return aquisi��o do controle para registrar acesso ao banco de dados.
	 */

	public LoginLogControl getLoginLogControl()
	{
		return logControl;
	}

	public IpBanControl getIpBanControl()
	{
		return ipbanControl;
	}

	/**
	 * @return aquisi��o do controle para identificar jogadores online.
	 */

	public OnlineControl getOnlineControl()
	{
		return onlineControl;
	}

	/**
	 * @return aquisi��o do controle para autentica��o dos jogadores.
	 */

	public AuthControl getAuthControl()
	{
		return authControl;
	}

	/**
	 * Cria todas as inst�ncias dos servi�os e controles para um servidor de acesso.
	 * @param loginServer refer�ncia do servidor de acesso que ir� us�-los.
	 */

	public void create(LoginServer loginServer)
	{
		accountControl = new AccountControl(loginServer.getMySQL().getConnection());
		groupControl = new GroupControl(loginServer.getMySQL().getConnection());
		pincodeControl = new PincodeControl(loginServer.getMySQL().getConnection());
		logControl = new LoginLogControl(loginServer.getMySQL().getConnection());
		ipbanControl = new IpBanControl(loginServer.getMySQL().getConnection());
		onlineControl = new OnlineControl(loginServer.getTimerSystem().getTimers());
		authControl = new AuthControl();

		accountControl.setGroupControl(groupControl);
		accountControl.setPincodeControl(pincodeControl);

		accountService = new ServiceLoginAccount(loginServer);
		authService = new ServiceLoginAuth(loginServer);
		charService = new ServiceLoginChar(loginServer);
		clientService = new ServiceLoginClient(loginServer);
		ipBanService = new ServiceLoginIpBan(loginServer);
		logService = new ServiceLoginLog(loginServer);
		loginService = new ServiceLoginServer(loginServer);

		loginService.init();
		charService.init();
		clientService.init();
		accountService.init();
		authService.init();

		if (loginServer.getConfigs().getBool(LOG_LOGIN))
			logService.init();

		if (loginServer.getConfigs().getBool(IPBAN_ENABLED))
			ipBanService.init();		
	}

	/**
	 * Procedimento de preparo para a destrui��o deste fa�ade utilizado pelo servidor de acesso passado.
	 * Deve fechar todas as conex�es com os servidores de personagem que estiverem estabelecidas.
	 * Tamb�m deve destruir todos os servi�os e controles que est�o aqui instanciados.
	 * @param loginServer servidor de acesso que est� chamando essa opera��o do fa�ade.
	 */

	public void destroy(LoginServer loginServer)
	{
		logService.destroy();
		ipBanService.destroy();
		authService.destroy();
		accountService.destroy();
		clientService.destroy();
		charService.destroy();
		loginService.destroy();

		if (loginServer.getConfigs().getBool(IPBAN_ENABLED))
			ipBanService.destroy();

		groupControl.clear();
		onlineControl.clear();
		authControl.clear();
		ipbanControl.clear();		
	}

	/**
	 * Destr�i todos os servi�os e controles removendo a refer�ncia de seus objetos.
	 */

	public void destroyed()
	{
		groupControl = null;
		onlineControl = null;
		authControl = null;
		ipbanControl = null;

		logService = null;
		ipBanService = null;
		authService = null;
		accountService = null;
		clientService = null;
		charService = null;
		loginService = null;		
	}
}

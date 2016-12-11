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
 * <h1>Servidor de Acesso - Façade</h1>
 *
 * <p>Essa classe é usada para centralizar todos os serviços e controles do servidor de acesso.
 * Através dele os serviços poderão comunicar-se entre si como também chamar os controles disponíveis.
 * Possui métodos que irão realizar a criação das instâncias e destruição das mesmas quando necessário.</p>
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
	 * Serviço para comunicação com o servidor de personagens.
	 */
	private ServiceLoginChar charService;

	/**
	 * Serviço para comunicação entre o servidor e o cliente.
	 */
	private ServiceLoginClient clientService;

	/**
	 * Serviço para banimento de acessos por endereço de IP.
	 */
	private ServiceLoginIpBan ipBanService;

	/**
	 * Serviço para registro de acessos.
	 */
	private ServiceLoginLog logService;

	/**
	 * Serviço para acesso de contas (serviço principal)
	 */
	private ServiceLoginServer loginService;

	/**
	 * Serviço para autenticação de acessos.
	 */
	private ServiceLoginAuth authService;

	/**
	 * Serviço para trabalhar com as contas dos jogadores.
	 */
	private ServiceLoginAccount accountService;


	/**
	 * Controle para persistência das contas de jogadores.
	 */
	private AccountControl accountControl;

	/**
	 * Controle para persistência e cache dos grupos de jogadores.
	 */
	private GroupControl groupControl;

	/**
	 * Controle para persistência dos código PIN das contas de jogadores.
	 */
	private PincodeControl pincodeControl;

	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	private LoginLogControl logControl;

	/**
	 * Controle para banimento de endereços de IP.
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
	 * @return aquisição do serviço para comunicação com o servidor de personagens.
	 */

	public ServiceLoginChar getCharService()
	{
		return charService;
	}

	/**
	 * @return aquisição do serviço para comunicação do servidor com o cliente.
	 */

	public ServiceLoginClient getClientService()
	{
		return clientService;
	}

	/**
	 * @return aquisição do serviço para banimento de acessos por endereço de IP.
	 */

	public ServiceLoginIpBan getIpBanService()
	{
		return ipBanService;
	}

	/**
	 * @return aquisição do serviço para registro de acessos no servidor.
	 */

	public ServiceLoginLog getLogService()
	{
		return logService;
	}

	/**
	 * @return aquisição do serviço para acesso de contas (serviço principal)
	 */

	public ServiceLoginServer getLoginService()
	{
		return loginService;
	}

	/**
	 * @return aquisição do serviço para autenticação de acessos.
	 */

	public ServiceLoginAuth getAuthService()
	{
		return authService;
	}

	/**
	 * @return aquisição do serviço para trabalhar com as contas dos jogadores.
	 */

	public ServiceLoginAccount getAccountService()
	{
		return accountService;
	}

	/**
	 * @return aquisição do controle para gerenciar as contas dos jogadores.
	 */

	public AccountControl getAccountControl()
	{
		return accountControl;
	}

	/**
	 * @return aquisição do controle para gerenciar os grupos de jogadores.
	 */

	public GroupControl getGroupControl()
	{
		return groupControl;
	}

	/**
	 * @return aquisição do controle para gerenciar os códigos PIN de contas.
	 */

	public PincodeControl getPincodeControl()
	{
		return pincodeControl;
	}

	/**
	 * @return aquisição do controle para registrar acesso ao banco de dados.
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
	 * @return aquisição do controle para identificar jogadores online.
	 */

	public OnlineControl getOnlineControl()
	{
		return onlineControl;
	}

	/**
	 * @return aquisição do controle para autenticação dos jogadores.
	 */

	public AuthControl getAuthControl()
	{
		return authControl;
	}

	/**
	 * Cria todas as instâncias dos serviços e controles para um servidor de acesso.
	 * @param loginServer referência do servidor de acesso que irá usá-los.
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
	 * Procedimento de preparo para a destruição deste façade utilizado pelo servidor de acesso passado.
	 * Deve fechar todas as conexões com os servidores de personagem que estiverem estabelecidas.
	 * Também deve destruir todos os serviços e controles que estão aqui instanciados.
	 * @param loginServer servidor de acesso que está chamando essa operação do façade.
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
	 * Destrói todos os serviços e controles removendo a referência de seus objetos.
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

package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.configs.JRagnarokConfigs.IPBAN_ENABLED;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOG_LOGIN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_CHARSERVERCONNECT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_CONNECT_INFO_CHANGED;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_EXE_HASHCHECK;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN2;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN3;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN4;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN_HAN;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_LOGIN_PCBANG;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_REQ_HASH;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CA_SSO_LOGIN_REQ;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_STATE_NOTIFY;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_STATE_UPDATE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_NOTIFY_PIN_ERROR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_NOTIFY_PIN_UPDATE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_DATA;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_ACCOUNT_INFO;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_AUTH_ACCOUNT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_CHANGE_EMAIL;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_GLOBAL_REGISTERS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_UNBAN_ACCOUNT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_VIP_DATA;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_KEEP_ALIVE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_SEND_ACCOUNTS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_SET_ACCOUNT_OFFLINE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_SET_ACCOUNT_ONLINE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_SET_ALL_ACC_OFFLINE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_UPDATE_IP;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_UPDATE_REGISTERS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HA_UPDATE_USER_COUNT;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.AcknowledgePacket;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.GlobalRegisterControl;
import org.diverproject.jragnarok.server.login.control.GroupControl;
import org.diverproject.jragnarok.server.login.control.IpBanControl;
import org.diverproject.jragnarok.server.login.control.LoginLogControl;
import org.diverproject.jragnarok.server.login.control.PincodeControl;
import org.diverproject.util.lang.HexUtil;

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
	 * Controle para registros de vari�veis global.
	 */
	private GlobalRegisterControl globalRegistersControl;

	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineMap onlineMap;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthAccountMap authAccountMap;

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

	/**
	 * @return aquisi��o do controle para banimento de endere�os IP.
	 */

	public IpBanControl getIpBanControl()
	{
		return ipbanControl;
	}

	/**
	 * @return aquisi��o do controle para registro de vari�veis globais.
	 */

	public GlobalRegisterControl getGlobalRegistersControl()
	{
		return globalRegistersControl;
	}

	/**
	 * @return aquisi��o do controle para identificar jogadores online.
	 */

	public OnlineMap getOnlineMap()
	{
		return onlineMap;
	}

	/**
	 * @return aquisi��o do controle para autentica��o dos jogadores.
	 */

	public AuthAccountMap getAuthAccountMap()
	{
		return authAccountMap;
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
		globalRegistersControl = new GlobalRegisterControl(loginServer.getMySQL().getConnection());
		onlineMap = new OnlineMap();
		authAccountMap = new AuthAccountMap();

		accountControl.setGroupControl(groupControl);
		accountControl.setPincodeControl(pincodeControl);
		groupControl.init();

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
		groupControl.destroy();
		ipBanService.destroy();
		authService.destroy();
		accountService.destroy();
		clientService.destroy();
		charService.destroy();
		loginService.destroy();

		if (loginServer.getConfigs().getBool(LOG_LOGIN))
			logService.destroy();

		if (loginServer.getConfigs().getBool(IPBAN_ENABLED))
			ipBanService.destroy();

		onlineMap.clear();
		authAccountMap.clear();
		ipbanControl.clear();
	}

	/**
	 * Destr�i todos os servi�os e controles removendo a refer�ncia de seus objetos.
	 */

	public void destroyed()
	{
		accountControl = null;
		groupControl = null;
		pincodeControl = null;
		logControl = null;
		ipbanControl = null;
		globalRegistersControl = null;
		onlineMap = null;
		authAccountMap = null;

		logService = null;
		ipBanService = null;
		authService = null;
		accountService = null;
		clientService = null;
		charService = null;
		loginService = null;		
	}

	/**
	 * Listener usado para receber novas conex�es solicitadas com o servidor de acesso.
	 * A an�lise verifica se a conex�o j� foi feita e se tiver verifica se est� banido.
	 * Caso n�o esteja banido ou n�o haja conex�o estabelece uma nova conex�o.
	 */

	public final FileDescriptorListener PARSE_CLIENT = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			LFileDescriptor lfd = (LFileDescriptor) fd;

			if (!fd.isConnected())
				return false;

			// J� conectou, verificar se est� banido
			if (lfd.getSessionData().getCache() == null)
				if (!ipBanService.parseBanTime(lfd))
					return true;

			logDebug("recebendo pacote de um cliente (fd: %d).\n", fd.getID());

			return ackClientPacket(lfd);
		}
	};

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * Neste caso ir� identificar o comando e que � esperado como fase inicial do cliente.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	public boolean ackClientPacket(LFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd);

		short command = packetReceivePacketID.getPacketID();

		switch (command)
		{
			case PACKET_CA_CONNECT_INFO_CHANGED:
				clientService.keepAlive(fd);
				return true;

			case PACKET_CA_EXE_HASHCHECK:
				clientService.updateClientHash(fd);
				return true;

			case PACKET_CA_REQ_HASH:
				clientService.parseRequestKey(fd);
				return true;
		}

		return dispatchAuthPacket(fd, command);
	}

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * Neste caso o comando j� est� identificado e dever� apenas despachar a conex�o.
	 * Os comandos aqui s�o de acesso inicial dos clientes com o servidor de acesso.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @param command comando que est� sendo solicitado (c�digo do pacote recebido).
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	public boolean dispatchAuthPacket(LFileDescriptor fd, short command)
	{
		switch (command)
		{
			// Solicita��o de acesso com senha direta
			case PACKET_CA_LOGIN:
			case PACKET_CA_LOGIN_PCBANG:
			case PACKET_CA_LOGIN_HAN:
			case PACKET_CA_SSO_LOGIN_REQ:
			// Solicita��o de acesso com senha MD5
			case PACKET_CA_LOGIN2:
			case PACKET_CA_LOGIN3:
			case PACKET_CA_LOGIN4:
				return authService.requestAuth(fd, command);

			case PACKET_HA_CHARSERVERCONNECT:
				return authService.requestCharConnect(fd);

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logWarning("pacote desconhecido recebido (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	/**
	 * Listener usado para receber novas conex�es solicitadas com o servidor de acesso.
	 * A an�lise � feita apenas para as conex�es j� autorizadas dos servidores de personagem.
	 * Sempre que autorizado e receber um pacote do mesmo deve ser passado por aqui.
	 */

	public final FileDescriptorListener PARSE_CHAR_SERVER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			LFileDescriptor lfd = (LFileDescriptor) fd;

			if (!fd.isConnected())
				return false;

			logDebug("recebendo pacote de um servidor de personagem (fd: %d).\n", fd.getID());

			return ackCharServerPacket(lfd);
		}
	};

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * Neste caso o comando j� est� identificado e dever� apenas despachar a conex�o.
	 * Os comandos aqui s�o b�sicos entre os servidores de personagem com o de acesso.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @param command comando que est� sendo solicitado (c�digo do pacote recebido).
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean ackCharServerPacket(LFileDescriptor fd)
	{
		AcknowledgePacket packet = new AcknowledgePacket();
		packet.receive(fd, false);

		short command = packet.getPacketID();

		switch (command)
		{
			case PACKET_HA_UPDATE_USER_COUNT:
				charService.updateUserCount(fd);
				return true;

			case PACKET_HA_KEEP_ALIVE:
				clientService.pingCharRequest(fd);
				return true;

			case PACKET_HA_SET_ACCOUNT_ONLINE:
				charService.setAccountOnline(fd);
				return true;

			case PACKET_HA_SET_ACCOUNT_OFFLINE:
				charService.setAccountOffline(fd);
				return true;

			case PACKET_HA_SEND_ACCOUNTS:
				charService.receiveSentAccounts(fd);
				return true;

			case PACKET_HA_SET_ALL_ACC_OFFLINE:
				charService.setAllOffline(fd);
				return true;

			default:
				return dispatchAccountManagerPacket(fd, command);
		}
	}

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * Neste caso o comando j� est� identificado e dever� apenas despachar a conex�o.
	 * Al�m disso os comandos aqui s�o esperados que sejam de um servidor de personagem.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @param command comando que est� sendo solicitado (c�digo do pacote recebido).
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	public boolean dispatchAccountManagerPacket(LFileDescriptor fd, short command)
	{
		switch (command)
		{
			case PACKET_HA_AUTH_ACCOUNT:
				charService.requestAuthAccount(fd);
				return true;

			case PACKET_HA_ACCOUNT_DATA:
				charService.requestAccountData(fd);
				return true;

			case PACKET_HA_ACCOUNT_INFO:
				charService.requestAccountInfo(fd);
				return true;

			case PACKET_HA_UPDATE_IP:
				charService.updateCharIP(fd);
				return true;

			case PACKET_HA_VIP_DATA:
				charService.requestVipData(fd);
				return true;

			default:
				return dispatchPlayerAccountPacket(fd, command);
		}
	}

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * Neste caso o comando j� est� identificado e dever� apenas despachar a conex�o.
	 * Al�m disso os comandos aqui s�o esperados que sejam de um servidor de personagem.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @param command comando que est� sendo solicitado (c�digo do pacote recebido).
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	public boolean dispatchPlayerAccountPacket(LFileDescriptor fd, short command)
	{
		switch (command)
		{
			case PACKET_HA_CHANGE_EMAIL:
				accountService.requestChangeEmail(fd);
				return true;

			case PACKET_HA_ACCOUNT_STATE_UPDATE:
				accountService.updateAccountState(fd);
				return true;

			case PACKET_HA_ACCOUNT_STATE_NOTIFY:
				accountService.requestBanAccount(fd);
				return true;

			case PACKET_HA_UPDATE_REGISTERS:
				accountService.updateGlobalRegister(fd);
				return true;

			case PACKET_HA_UNBAN_ACCOUNT:
				accountService.requestUnbanAccount(fd);
				return true;

			case PACKET_HA_GLOBAL_REGISTERS:
				accountService.requestRegister(fd);
				return true;

			case PACKET_HA_NOTIFY_PIN_UPDATE:
				accountService.updatePinCode(fd);
				return true;

			case PACKET_HA_NOTIFY_PIN_ERROR:
				accountService.failPinCode(fd);
				return true;

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logWarning("pacote desconhecido recebido (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}
}

package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.emailCheck;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.time;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AccountStateUpdate;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_BanAccount;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_ChangeEmail;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_GlobalRegisters;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_NotifyPinError;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_NotifyPinUpdate;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_UnbanAccount;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_UpdateRegisters;
import org.diverproject.jragnarok.server.common.GlobalRegister;
import org.diverproject.jragnarok.server.common.GlobalRegisterOperation;
import org.diverproject.jragnarok.server.common.LoginAdapt;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.GlobalRegisterControl;
import org.diverproject.jragnarok.server.login.control.LoginLogControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountState;
import org.diverproject.jragnarok.server.login.entities.LoginLog;
import org.diverproject.util.collection.Queue;

/**
 * <h1>Serviço para Gerenciamento de Contas</h1>
 *
 * <p>Serviço utilizado no servidor de acesso para gerenciar as requisições (pacotes) relativo a contas.
 * Todas as requisições aqui feitas através da comunicação entre o servidor de personagem e o de acesso.
 * Após receber as informações contidas nos pacotes deverá realizar a operação adequada para tal.</p>
 *
 * <p>As funcionalidades disponíveis neste serviço são exclusivamente referentes a conta de um jogador.
 * Pode ser solicitação de informações como também a atualização delas e até mesmo registrar novas informações.
 * Além das informações básicas dos personagens será trabalhado os registros (variáveis) e estado (online/offline).</p>
 *
 * @see AbstractServiceLogin
 * @see ServiceLoginClient
 * @see AccountControl
 * @see AuthAccountMap
 * @see LoginServer
 *
 * @author Andrew
 */

public class ServiceLoginAccount extends AbstractServiceLogin
{
	/**
	 * Serviço para comunicação entre o servidor e o cliente.
	 */
	private ServiceLoginClient client;

	/**
	 * Serviço para acesso de contas (serviço principal)
	 */
	private ServiceLoginServer login;

	/**
	 * Controle para persistência das contas de jogadores.
	 */
	private AccountControl accounts;

	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	private LoginLogControl logs;

	/**
	 * Controle para registros de variáveis global.
	 */
	private GlobalRegisterControl globalRegisters;

	/**
	 * Cria uma nova instância do serviço para gerenciamento de contas.
	 * @param server referência do servidor de acesso que irá usá-lo.
	 */

	public ServiceLoginAccount(LoginServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getClientService();
		login = getServer().getFacade().getLoginService();
		accounts = getServer().getFacade().getAccountControl();
		logs = getServer().getFacade().getLoginLogControl();
		globalRegisters = getServer().getFacade().getGlobalRegistersControl();
	}

	@Override
	public void destroy()
	{
		client = null;
		login = null;
		accounts = null;
		logs = null;
		globalRegisters = null;
	}

	/**
	 * Solicitação para alterar o endereço de e-mail de uma determinada conta no sistema.
	 * Recebe um pacote que tem o ID da conta como endereço de e-mail atual e novo.
	 * @param fd referência da sessão da conexão com o cliente da solicitação.
	 */

	public void requestChangeEmail(LFileDescriptor fd)
	{
		HA_ChangeEmail packet = new HA_ChangeEmail();
		packet.receive(fd);

		if (!emailCheck(packet.getActualEmail()))
			logWarning("endereço de e-mail atual inválido (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else if (!emailCheck(packet.getNewEmail()))
			logWarning("novo endereço de e-mail inválido (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else if (packet.getNewEmail().equals("a@a.com"))
			logWarning("novo endereço de e-mail como padrão (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else
		{
			Account account = accounts.get(packet.getAccountID());

			if (account == null)
				logWarning("conta não encontrada (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

			else
			{
				account.setEmail(packet.getNewEmail());

				if (accounts.set(account))
					logNotice("endereço de e-mail trocado para '%s' (account: %d, ip: %s).\n", packet.getNewEmail(), packet.getAccountID(), fd.getAddressString());
				else
					logWarning("falha ao persistir conta (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());
			}
		}
	}

	/**
	 * Solicitação para alteração do estado de uma determinada conta no sistema.
	 * Recebe um pacote que tem o ID da conta e o novo estado a ser assumido.
	 * Notifica a todas as conexões estabelecidas sobre a alteração do estado.
	 * @param fd referência da sessão da conexão com o cliente da solicitação.
	 */

	public void updateAccountState(LFileDescriptor fd)
	{
		HA_AccountStateUpdate packet = new HA_AccountStateUpdate();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());

		if (account == null)
			logWarning("conta não encontrada (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else if (account.getState() == packet.getAccountState())
			logNotice("conta já está em '%s' (account: %d, ip: %s).\n", packet.getAccountState(), packet.getAccountID(), fd.getAddressString());

		else
		{
			AccountState old = account.getState();
			account.setState(packet.getAccountState());

			if (!accounts.set(account))
				logWarning("falha na troca de estado (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

			else
			{
				logNotice("alterado de '%s' para '%s' (account: %d, ip: %s).\n", old, packet.getAccountState(), packet.getAccountID(), fd.getAddressString());
				client.sendAccountStateNotify(fd, account, false);
			}
		}
	}

	/**
	 * Solicitação para banir uma determinada conta por um certo tempo no sistema.
	 * Recebe um pacote que tem o ID da conta e o tempo em milissegundos do ban.
	 * Notifica a todas as conexões estabelecidas sobre o banimento da conta.
	 * @param fd referência da sessão da conexão com o cliente da solicitação.
	 */

	public void requestBanAccount(LFileDescriptor fd)
	{
		HA_BanAccount packet = new HA_BanAccount();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());

		if (account == null)
			logWarning("conta não encontrada (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else
		{
			long time = account.getUnban().get();
			time += time == 0 ? 0 : packet.getDurationTime();

			account.setState(AccountState.BANNED);
			account.getUnban().set(time);

			if (!accounts.set(account))
				logWarning("falha ao banir conta (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

			else
			{
				logNotice("'%s' banido por '%s' (ip: %s).\n", account.getUsername(), time(packet.getDurationTime()), fd.getAddressString());
				client.sendAccountStateNotify(fd, account, true);
			}
		}
	}

	/**
	 * Recebe de um servidor de personagem uma fila de registros com variáveis globais para serem atualizados.
	 * Cada registro será composto por uma chave de identificação seguido da operação e o seu valor.
	 * Os registros recebidos serão respectivos uma única conta de jogador identificado pelo seu código.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	@SuppressWarnings("unchecked")
	public void updateGlobalRegister(LFileDescriptor fd)
	{
		HA_UpdateRegisters packet = new HA_UpdateRegisters();
		packet.receive(fd);

		Queue<GlobalRegisterOperation<?>> registers = packet.getRegisters();

		while (!registers.isEmpty())
		{
			GlobalRegisterOperation<?> operation = registers.poll();

			if (operation.getRegister() == null)
				continue;

			try {

				switch (operation.getOperation())
				{
					case HA_UpdateRegisters.OPERATION_INT_REPLACE:
						GlobalRegister<Integer> registerIntReplace = (GlobalRegister<Integer>) operation.getRegister();
						registerIntReplace.setValue((Integer) registerIntReplace.getValue());
						globalRegisters.replace(registerIntReplace);
						break;

					case HA_UpdateRegisters.OPERATION_INT_DELETE:
						GlobalRegister<Integer> registerIntDelete = (GlobalRegister<Integer>) operation.getRegister();
						registerIntDelete.setValue((Integer) registerIntDelete.getValue());
						globalRegisters.delete(registerIntDelete);
						break;

					case HA_UpdateRegisters.OPERATION_STR_REPLACE:
						GlobalRegister<String> registerStrReplace = (GlobalRegister<String>) operation.getRegister();
						registerStrReplace.setValue((String) registerStrReplace.getValue());
						globalRegisters.replace(registerStrReplace);
						break;

					case HA_UpdateRegisters.OPERATION_STR_DELETE:
						GlobalRegister<String> registerStrDelete = (GlobalRegister<String>) operation.getRegister();
						registerStrDelete.setValue((String) registerStrDelete.getValue());
						globalRegisters.delete(registerStrDelete);
						break;

					default:
						logWarning("operação '%d' não encontrada.\n", operation.getOperation());
				}

			} catch (RagnarokException e) {

				logError("falha ao atualizar registro global (aid: %d, key: %s)", operation.getRegister().getAccountID(), operation.getRegister().getKey());
				logException(e);
			}
		}
	}

	/**
	 * Chamado quando um servidor de personagem solicitar a busca das variáveis globais de uma conta.
	 * Após receber o código da conta que será carregado envia o resultado contendo as variáveis.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void requestRegister(LFileDescriptor fd)
	{
		HA_GlobalRegisters packet = new HA_GlobalRegisters();
		packet.receive(fd);

		int accountID = packet.getAccountID();
		int charID = packet.getCharID();

		try {

			Queue<GlobalRegister<?>> registers = globalRegisters.getAll(accountID);
			client.sendGlobalRegisters(fd, accountID, charID, registers);

		} catch (RagnarokException e) {
			logError("falha ao obter variáveis globais (aid: %d):", accountID);
			logException(e);
		}
	}

	/**
	 * Recebe uma solicitação de um servidor de personagem para cancelar o banimento de uma determinada conta.
	 * O procedimento é feito recebendo o código de identificação da conta e restabelecendo o ban para null.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void requestUnbanAccount(LFileDescriptor fd)
	{
		HA_UnbanAccount packet = new HA_UnbanAccount();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());

		if (account == null)
			logWarning("conta não encontrada para cancelar banimento (aid: %d).\n", packet.getAccountID());

		else if (account.getUnban().get() == 0)
			logNotice("conta não possui banimento para ser cancelado (aid: %d).\n", packet.getAccountID());

		else
		{
			account.getUnban().set(0);
			accounts.set(account);

			logNotice("banimento de conta cancelado (aid: %d).\n", packet.getAccountID());
		}
	}

	/**
	 * Recebe uma solicitação de um servidor de personagem para realizar uma atualização no código pin.
	 * O procedimento é feito recebendo o código de identificação da conta alvo e o novo código pin.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void pincodeUpdate(LFileDescriptor fd)
	{
		HA_NotifyPinUpdate packet = new HA_NotifyPinUpdate();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());

		if (account == null)
			logWarning("conta não encontrada para atualizar código pin (aid: %d).\n", packet.getAccountID());

		else
		{
			account.getPincode().setCode(packet.getPincode());
			accounts.set(account);

			logNotice("código pin de conta atualizado (aid: %d, pincode: %s).\n", packet.getAccountID(), packet.getPincode());
		}
	}

	/**
	 * Recebe uma solicitação de um servidor de personagem para registrar uma autenticação de código pin falho.
	 * O procedimento é feito recebendo somente o código de identificação da conta que falhou no mesmo.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void pincodeFailure(LFileDescriptor fd)
	{
		HA_NotifyPinError packet = new HA_NotifyPinError();
		packet.receive(fd);

		if (accounts.exist(packet.getAccountID()) && login.isOnline(packet.getAccountID()))
		{
			try {

				LoginLog log = new LoginLog();
				log.setLogin(new LoginAdapt(packet.getAccountID()));
				log.setMessage("Falha na verificação do código PIN");
				log.setRCode(100);
				logs.add(log);

			} catch (RagnarokException e) {
				logError("falha durante o registro da falha no código pin (aid: %d).\n", packet.getAccountID());
				logException(e);
			}
		}

		login.removeOnlineUser(packet.getAccountID());
	}
}

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
 * <h1>Servi�o para Gerenciamento de Contas</h1>
 *
 * <p>Servi�o utilizado no servidor de acesso para gerenciar as requisi��es (pacotes) relativo a contas.
 * Todas as requisi��es aqui feitas atrav�s da comunica��o entre o servidor de personagem e o de acesso.
 * Ap�s receber as informa��es contidas nos pacotes dever� realizar a opera��o adequada para tal.</p>
 *
 * <p>As funcionalidades dispon�veis neste servi�o s�o exclusivamente referentes a conta de um jogador.
 * Pode ser solicita��o de informa��es como tamb�m a atualiza��o delas e at� mesmo registrar novas informa��es.
 * Al�m das informa��es b�sicas dos personagens ser� trabalhado os registros (vari�veis) e estado (online/offline).</p>
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
	 * Servi�o para comunica��o entre o servidor e o cliente.
	 */
	private ServiceLoginClient client;

	/**
	 * Servi�o para acesso de contas (servi�o principal)
	 */
	private ServiceLoginServer login;

	/**
	 * Controle para persist�ncia das contas de jogadores.
	 */
	private AccountControl accounts;

	/**
	 * Controle para registrar acesso ao banco de dados.
	 */
	private LoginLogControl logs;

	/**
	 * Controle para registros de vari�veis global.
	 */
	private GlobalRegisterControl globalRegisters;

	/**
	 * Cria uma nova inst�ncia do servi�o para gerenciamento de contas.
	 * @param server refer�ncia do servidor de acesso que ir� us�-lo.
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
	 * Solicita��o para alterar o endere�o de e-mail de uma determinada conta no sistema.
	 * Recebe um pacote que tem o ID da conta como endere�o de e-mail atual e novo.
	 * @param fd refer�ncia da sess�o da conex�o com o cliente da solicita��o.
	 */

	public void requestChangeEmail(LFileDescriptor fd)
	{
		HA_ChangeEmail packet = new HA_ChangeEmail();
		packet.receive(fd);

		if (!emailCheck(packet.getActualEmail()))
			logWarning("endere�o de e-mail atual inv�lido (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else if (!emailCheck(packet.getNewEmail()))
			logWarning("novo endere�o de e-mail inv�lido (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else if (packet.getNewEmail().equals("a@a.com"))
			logWarning("novo endere�o de e-mail como padr�o (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else
		{
			Account account = accounts.get(packet.getAccountID());

			if (account == null)
				logWarning("conta n�o encontrada (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

			else
			{
				account.setEmail(packet.getNewEmail());

				if (accounts.set(account))
					logNotice("endere�o de e-mail trocado para '%s' (account: %d, ip: %s).\n", packet.getNewEmail(), packet.getAccountID(), fd.getAddressString());
				else
					logWarning("falha ao persistir conta (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());
			}
		}
	}

	/**
	 * Solicita��o para altera��o do estado de uma determinada conta no sistema.
	 * Recebe um pacote que tem o ID da conta e o novo estado a ser assumido.
	 * Notifica a todas as conex�es estabelecidas sobre a altera��o do estado.
	 * @param fd refer�ncia da sess�o da conex�o com o cliente da solicita��o.
	 */

	public void updateAccountState(LFileDescriptor fd)
	{
		HA_AccountStateUpdate packet = new HA_AccountStateUpdate();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());

		if (account == null)
			logWarning("conta n�o encontrada (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

		else if (account.getState() == packet.getAccountState())
			logNotice("conta j� est� em '%s' (account: %d, ip: %s).\n", packet.getAccountState(), packet.getAccountID(), fd.getAddressString());

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
	 * Solicita��o para banir uma determinada conta por um certo tempo no sistema.
	 * Recebe um pacote que tem o ID da conta e o tempo em milissegundos do ban.
	 * Notifica a todas as conex�es estabelecidas sobre o banimento da conta.
	 * @param fd refer�ncia da sess�o da conex�o com o cliente da solicita��o.
	 */

	public void requestBanAccount(LFileDescriptor fd)
	{
		HA_BanAccount packet = new HA_BanAccount();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());

		if (account == null)
			logWarning("conta n�o encontrada (account: %d, ip: %s).\n", packet.getAccountID(), fd.getAddressString());

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
	 * Recebe de um servidor de personagem uma fila de registros com vari�veis globais para serem atualizados.
	 * Cada registro ser� composto por uma chave de identifica��o seguido da opera��o e o seu valor.
	 * Os registros recebidos ser�o respectivos uma �nica conta de jogador identificado pelo seu c�digo.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
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
						logWarning("opera��o '%d' n�o encontrada.\n", operation.getOperation());
				}

			} catch (RagnarokException e) {

				logError("falha ao atualizar registro global (aid: %d, key: %s)", operation.getRegister().getAccountID(), operation.getRegister().getKey());
				logException(e);
			}
		}
	}

	/**
	 * Chamado quando um servidor de personagem solicitar a busca das vari�veis globais de uma conta.
	 * Ap�s receber o c�digo da conta que ser� carregado envia o resultado contendo as vari�veis.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
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
			logError("falha ao obter vari�veis globais (aid: %d):", accountID);
			logException(e);
		}
	}

	/**
	 * Recebe uma solicita��o de um servidor de personagem para cancelar o banimento de uma determinada conta.
	 * O procedimento � feito recebendo o c�digo de identifica��o da conta e restabelecendo o ban para null.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void requestUnbanAccount(LFileDescriptor fd)
	{
		HA_UnbanAccount packet = new HA_UnbanAccount();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());

		if (account == null)
			logWarning("conta n�o encontrada para cancelar banimento (aid: %d).\n", packet.getAccountID());

		else if (account.getUnban().get() == 0)
			logNotice("conta n�o possui banimento para ser cancelado (aid: %d).\n", packet.getAccountID());

		else
		{
			account.getUnban().set(0);
			accounts.set(account);

			logNotice("banimento de conta cancelado (aid: %d).\n", packet.getAccountID());
		}
	}

	/**
	 * Recebe uma solicita��o de um servidor de personagem para realizar uma atualiza��o no c�digo pin.
	 * O procedimento � feito recebendo o c�digo de identifica��o da conta alvo e o novo c�digo pin.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void pincodeUpdate(LFileDescriptor fd)
	{
		HA_NotifyPinUpdate packet = new HA_NotifyPinUpdate();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());

		if (account == null)
			logWarning("conta n�o encontrada para atualizar c�digo pin (aid: %d).\n", packet.getAccountID());

		else
		{
			account.getPincode().setCode(packet.getPincode());
			accounts.set(account);

			logNotice("c�digo pin de conta atualizado (aid: %d, pincode: %s).\n", packet.getAccountID(), packet.getPincode());
		}
	}

	/**
	 * Recebe uma solicita��o de um servidor de personagem para registrar uma autentica��o de c�digo pin falho.
	 * O procedimento � feito recebendo somente o c�digo de identifica��o da conta que falhou no mesmo.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
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
				log.setMessage("Falha na verifica��o do c�digo PIN");
				log.setRCode(100);
				logs.add(log);

			} catch (RagnarokException e) {
				logError("falha durante o registro da falha no c�digo pin (aid: %d).\n", packet.getAccountID());
				logException(e);
			}
		}

		login.removeOnlineUser(packet.getAccountID());
	}
}

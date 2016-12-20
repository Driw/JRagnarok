package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.emailCheck;
import static org.diverproject.jragnarok.JRagnarokUtil.time;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.request.BanAccountRequest;
import org.diverproject.jragnarok.packets.request.ChangeEmailRequest;
import org.diverproject.jragnarok.packets.request.GlobalRegistersRequest;
import org.diverproject.jragnarok.packets.request.UpdateAccountState;
import org.diverproject.jragnarok.packets.request.UpdateGlobalRegisters;
import org.diverproject.jragnarok.server.common.GlobalAccountReg;
import org.diverproject.jragnarok.server.common.GlobalRegister;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.GlobalRegisterControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountState;
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
	 * Controle para persist�ncia das contas de jogadores.
	 */
	private AccountControl accounts;

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
		accounts = getServer().getFacade().getAccountControl();
		globalRegisters = getServer().getFacade().getGlobalRegistersControl();
	}

	@Override
	public void destroy()
	{
		client = null;
		accounts = null;
		globalRegisters = null;
	}

	/**
	 * Solicita��o para alterar o endere�o de e-mail de uma determinada conta no sistema.
	 * Recebe um pacote que tem o ID da conta como endere�o de e-mail atual e novo.
	 * @param fd refer�ncia da sess�o da conex�o com o cliente da solicita��o.
	 */

	public void requestChangeEmail(LFileDescriptor fd)
	{
		ChangeEmailRequest packet = new ChangeEmailRequest();
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
		UpdateAccountState packet = new UpdateAccountState();
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
				client.sendNotifyAccountState(fd, account, false);
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
		BanAccountRequest packet = new BanAccountRequest();
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
				client.sendNotifyAccountState(fd, account, true);
			}
		}
	}

	/**
	 * Recebe de um servidor de personagem uma fila de registros com vari�veis globais para serem atualizados.
	 * Cada registro ser� composto por uma chave de identifica��o seguido da opera��o e o seu valor.
	 * Os registros recebidos ser�o respectivos uma �nica conta de jogador identificado pelo seu c�digo.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void updateGlobalRegister(LFileDescriptor fd)
	{
		UpdateGlobalRegisters packet = new UpdateGlobalRegisters();
		packet.receive(fd);

		int accountID = packet.getAccountID();
		Queue<GlobalAccountReg> registers = packet.getRegisters();

		while (!registers.isEmpty())
		{
			GlobalAccountReg globalRegister = registers.poll();

			try {

				switch (globalRegister.getOperation())
				{
					case UpdateGlobalRegisters.OPERATION_INT_REPLACE:
						GlobalRegister<Integer> registerIntReplace = new GlobalRegister<Integer>(accountID, globalRegister.getKey());
						registerIntReplace.setValue((Integer) globalRegister.getValue());
						globalRegisters.replace(accountID, registerIntReplace);
						break;

					case UpdateGlobalRegisters.OPERATION_INT_DELETE:
						GlobalRegister<Integer> registerIntDelete = new GlobalRegister<Integer>(accountID, globalRegister.getKey());
						registerIntDelete.setValue((Integer) globalRegister.getValue());
						globalRegisters.delete(accountID, registerIntDelete);
						break;

					case UpdateGlobalRegisters.OPERATION_STR_REPLACE:
						GlobalRegister<String> registerStrReplace = new GlobalRegister<String>(accountID, globalRegister.getKey());
						registerStrReplace.setValue((String) globalRegister.getValue());
						globalRegisters.replace(accountID, registerStrReplace);
						break;

					case UpdateGlobalRegisters.OPERATION_STR_DELETE:
						GlobalRegister<String> registerStrDelete = new GlobalRegister<String>(accountID, globalRegister.getKey());
						registerStrDelete.setValue((String) globalRegister.getValue());
						globalRegisters.delete(accountID, registerStrDelete);
						break;
				}

			} catch (RagnarokException e) {
				logError("falha ao atualizar registro global (aid: %d, key: %s)", accountID, globalRegister.getKey());
				logExeception(e);
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
		GlobalRegistersRequest packet = new GlobalRegistersRequest();
		packet.receive(fd);

		int accountID = packet.getAccountID();
		int charID = packet.getCharID();

		try {

			Queue<GlobalRegister<?>> registers = globalRegisters.getAll(accountID);
			client.sendGlobalRegisters(fd, accountID, charID, registers);

		} catch (RagnarokException e) {
			logError("falha ao obter vari�veis globais (aid: %d):", accountID);
			logExeception(e);
		}
	}

	/**
	 * TODO
	 * @param fd
	 */

	public void requestUnbanAccount(LFileDescriptor fd)
	{
		// TODO logchrif_parse_requnbanacc
		
	}

	/**
	 * TODO
	 * @param fd
	 * @return
	 */

	public void updatePinCode(LFileDescriptor fd)
	{
		// TODO logchrif_parse_updpincode
		
	}

	/**
	 * TODO
	 * @param fd
	 * @return
	 */

	public void failPinCode(LFileDescriptor fd)
	{
		// TODO logchrif_parse_pincode_authfail
		
	}

	/**
	 * TODO
	 * @param account
	 * @return
	 */

	public boolean newAccount(Account account)
	{
		// TODO login_mmo_auth_new
		return false;
	}
}

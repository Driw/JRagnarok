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
	 * Controle para persistência das contas de jogadores.
	 */
	private AccountControl accounts;

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
	 * Solicitação para alterar o endereço de e-mail de uma determinada conta no sistema.
	 * Recebe um pacote que tem o ID da conta como endereço de e-mail atual e novo.
	 * @param fd referência da sessão da conexão com o cliente da solicitação.
	 */

	public void requestChangeEmail(LFileDescriptor fd)
	{
		ChangeEmailRequest packet = new ChangeEmailRequest();
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
		UpdateAccountState packet = new UpdateAccountState();
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
				client.sendNotifyAccountState(fd, account, false);
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
		BanAccountRequest packet = new BanAccountRequest();
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
				client.sendNotifyAccountState(fd, account, true);
			}
		}
	}

	/**
	 * Recebe de um servidor de personagem uma fila de registros com variáveis globais para serem atualizados.
	 * Cada registro será composto por uma chave de identificação seguido da operação e o seu valor.
	 * Os registros recebidos serão respectivos uma única conta de jogador identificado pelo seu código.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
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
	 * Chamado quando um servidor de personagem solicitar a busca das variáveis globais de uma conta.
	 * Após receber o código da conta que será carregado envia o resultado contendo as variáveis.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
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
			logError("falha ao obter variáveis globais (aid: %d):", accountID);
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

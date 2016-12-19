package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.emailCheck;
import static org.diverproject.jragnarok.JRagnarokUtil.time;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnarok.packets.request.BanAccountRequest;
import org.diverproject.jragnarok.packets.request.ChangeEmailRequest;
import org.diverproject.jragnarok.packets.request.UpdateAccountState;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountState;

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
	}

	@Override
	public void destroy()
	{
		client = null;
		accounts = null;
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
	 * TODO
	 * @param fd
	 * @param accountID
	 * @param charID
	 */

	public void sendRegister(LFileDescriptor fd)
	{
		// TODO mmo_send_global_accreg
		
	}

	/**
	 * TODO
	 * @param fd
	 * @param accountID
	 * @param charID
	 */

	public void saveRegister(LFileDescriptor fd)
	{
		// TODO mmo_save_global_accreg
		
	}

	/**
	 * TODO
	 * @param fd
	 */

	public void updateRegister(LFileDescriptor fd)
	{
		// TODO logchrif_parse_upd_global_accreg
		
	}

	/**
	 * TODO
	 * @param fd
	 */

	public void requestRegister(LFileDescriptor fd)
	{
		// TODO logchrif_parse_req_global_accreg
		
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

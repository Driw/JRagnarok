package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.emailCheck;
import static org.diverproject.jragnarok.JRagnarokUtil.time;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnarok.packets.request.AccountDataRequest;
import org.diverproject.jragnarok.packets.request.AccountInfoRequest;
import org.diverproject.jragnarok.packets.request.AuthAccountRequest;
import org.diverproject.jragnarok.packets.request.BanAccountRequest;
import org.diverproject.jragnarok.packets.request.ChangeEmailRequest;
import org.diverproject.jragnarok.packets.request.UpdateAccountState;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountState;
import org.diverproject.jragnarok.server.login.structures.AuthNode;

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
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthAccountMap auths;

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
		auths = getServer().getFacade().getAuthControl();		
	}

	@Override
	public void destroy()
	{
		client = null;
		accounts = null;
		auths = null;		
	}

	/**
	 * Solicitação para concluir a autenticação de uma determinada conta já acessada.
	 * Cada autenticação pode ser usada uma única vez por cada acesso autorizado.
	 * @param fd referência da sessão da conexão com o cliente da solicitação.
	 */

	public void requestAuthAccount(LFileDescriptor fd)
	{
		AuthAccountRequest packet = new AuthAccountRequest();
		packet.receive(fd);

		AuthNode node = auths.get(packet.getAccountID());

		if (node != null &&
			node.getAccountID() == packet.getAccountID() &&
			node.getSeed().getFirst() == packet.getFirstSeed() &&
			node.getSeed().getSecond() == packet.getSecondSeed())
		{
			client.sendAuthAccount(fd, packet, node);
			auths.remove(node); // cada autenticação é usada uma só vez
		}

		else
		{
			logInfo("autenticação de conta RECUSADA (server-fd: %d, ufd: %d).\n", fd.getID(), packet.getFileDescriptorID());
			client.sendAuthAccount(fd, packet);
		}
	}

	/**
	 * Solicitação para obter dados básicos de uma conta especificado pelo seu ID.
	 * Recebe um pacote que terá alguns dados básicos recebidos do servidor.
	 * @param fd referência da sessão da conexão com o cliente da solicitação.
	 */

	public void requestAccountData(LFileDescriptor fd)
	{
		AccountDataRequest packet = new AccountDataRequest();
		packet.receive(fd);

		int id = packet.getAccountID();
		Account account = accounts.get(id);

		if (account == null)
			logNotice("conta #%d não encontrada (ip: %s).\n", id, fd.getAddressString());
		else
			client.sendAccountData(fd, account);
	}

	/**
	 * Solicitação para obter informações de uma conta especificada pelo seu ID.
	 * Recebe um pacote que terá alguns dados básicos recebidos do servidor.
	 * @param fd referência da sessão da conexão com o cliente da solicitação.
	 */

	public void requestAccountInfo(LFileDescriptor fd)
	{
		AccountInfoRequest packet = new AccountInfoRequest();

		int id = packet.getAccountID();
		Account account = accounts.get(id);
		client.sendAccountInfo(fd, packet, account);
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
	 */

	public void updateCharIP(LFileDescriptor fd)
	{
		// TODO logchrif_parse_updcharip
		
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
	 * @param fd
	 * @return
	 */

	public void requestVipData(LFileDescriptor fd)
	{
		// TODO logchrif_parse_reqvipdata
		
	}

	/**
	 * TODO
	 * @param fd
	 * @return
	 */

	public void setAccountOnline(LFileDescriptor fd)
	{
		// TODO logchrif_parse_setacconline
		
	}

	/**
	 * TODO
	 * @param fd
	 */

	public void setAccountOffline(LFileDescriptor fd)
	{
		// TODO logchrif_parse_setaccoffline
		
	}

	/**
	 * TODO
	 * @param fd
	 */

	public void setAllOffline(LFileDescriptor fd)
	{
		// TODO logchrif_parse_setalloffline
		
	}

	/**
	 * TODO
	 * @param fd
	 */

	public void updateOnlineDB(LFileDescriptor fd)
	{
		// TODO logchrif_parse_updonlinedb
		
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

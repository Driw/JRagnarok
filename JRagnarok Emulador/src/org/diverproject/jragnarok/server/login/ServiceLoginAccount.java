package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ACCOUNT_STATE_NOTIFY;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_CHANGE_EMAIL;
import static org.diverproject.jragnarok.JRagnarokUtil.emailCheck;
import static org.diverproject.jragnarok.JRagnarokUtil.time;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_ACCOUNT_DATA;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_ACCOUNT_INFO;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ACCOUNT_STATE_UPDATE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AUTH_ACCOUNT_REQ;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnarok.packets.receive.AuthAccountResponse;
import org.diverproject.jragnarok.packets.request.AccountDataRequest;
import org.diverproject.jragnarok.packets.request.AccountInfoRequest;
import org.diverproject.jragnarok.packets.request.BanAccountRequest;
import org.diverproject.jragnarok.packets.request.ChangeEmailRequest;
import org.diverproject.jragnarok.packets.request.UpdateAccountState;
import org.diverproject.jragnarok.packets.response.RefuseEnter;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.login.controllers.AccountControl;
import org.diverproject.jragnarok.server.login.controllers.AuthControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.AccountState;
import org.diverproject.jragnarok.server.login.entities.AuthNode;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.util.lang.HexUtil;

public class ServiceLoginAccount extends AbstractServiceLogin
{
	/**
	 * Controle para intermediar a persist�ncia de contas e cache de contas.
	 */
	private AccountControl accounts;

	/**
	 * Servi�o para comunica��o entre o cliente e servidor.
	 */
	private ServiceLoginClient client;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthControl auths;

	public ServiceLoginAccount(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa o servi�o para recebimento de novos clientes no servidor.
	 */

	public void init()
	{
		client = getServer().getClientService();

		accounts = getServer().getLoginService().getAccountControl();
		auths = getServer().getLoginService().getAuthControl();
	}

	public boolean dispatch(short command, FileDescriptor fd)
	{
		switch (command)
		{
			case PACKET_AUTH_ACCOUNT_REQ:
				requestAuthAccount(fd);
				return true;

			case PACKET_REQ_ACCOUNT_DATA:
				requestAccountData(fd);
				return true;

			case PACKET_REQ_ACCOUNT_INFO:
				requestAccountInfo(fd);
				return true;

			// case 0x272B: return account.logchrif_parse_setacconline(fd);
			// case 0x272C: return account.logchrif_parse_setaccoffline(fd);
			// case 0x272D: return account.logchrif_parse_updonlinedb(fd);
			// case 0x2737: return account.logchrif_parse_setalloffline(fd);

			default:
				return individualRequests(command, fd);
		}
	}

	private boolean individualRequests(short command, FileDescriptor fd)
	{
		switch (command)
		{
			case PACKET_REQ_CHANGE_EMAIL:
				ackChangeEmail(fd);
				return true;

			case PACKET_ACCOUNT_STATE_UPDATE:
				updateAccountState(fd);
				return true;

			case PACKET_ACCOUNT_STATE_NOTIFY:
				banAccount(fd);
				return true;

			// case 0x2725: return account.logchrif_parse_reqbanacc(fd);
			// case 0x2728: return account.logchrif_parse_upd_global_accreg(fd);
			// case 0x272A: return account.logchrif_parse_requnbanacc(fd);
			// case 0x272E: return account.logchrif_parse_req_global_accreg(fd);
			// case 0x2736: return account.logchrif_parse_updcharip(fd);
			// case 0x2738: return account.logchrif_parse_updpincode(fd);
			// case 0x2739: return account.logchrif_parse_pincode_authfail(fd);
			// case 0x2742: return account.logchrif_parse_reqvipdata(fd);
		}

		logWarning("pacote inesperado recebido (%s).\n", HexUtil.parseShort(command, 4));
		return false;
	}

	/**
	 * Solicita��o para concluir a autentica��o de uma determinada conta j� acessada.
	 * Cada autentica��o pode ser usada uma �nica vez por cada acesso autorizado.
	 * @param fd refer�ncia da sess�o da conex�o com o cliente da solicita��o.
	 */

	private void requestAuthAccount(FileDescriptor fd)
	{
		AuthAccountResponse packet = new AuthAccountResponse();
		packet.receive(fd);

		AuthNode node = auths.get(packet.getAccountID());
		ClientCharServer server = getServer().getCharServerList().get(fd);

		if (server != null &&
			node.getAccountID() == packet.getAccountID() &&
			node.getSeed().getFirst() == packet.getFirstSeed() &&
			node.getSeed().getSecond() == packet.getSecondSeed())
		{
			client.authAccount(fd, node, packet.getAccountID(), true);
			auths.remove(node); // cada autentica��o � usada uma s� vez
		}

		else if (server != null)
		{
			logInfo("autentica��o da conta '%s' RECUSADA (char-server: %s, ip: %s).\n", server.getName(), fd.getAddressString());
			client.authAccount(fd, node, packet.getAccountID(), false);
		}

		else
			client.refuseEnter(fd, RefuseEnter.REJECTED_FROM_SERVER);
	}

	/**
	 * Solicita��o para obter dados b�sicos de uma conta especificado pelo seu ID.
	 * Recebe um pacote que ter� alguns dados b�sicos recebidos do servidor.
	 * @param fd refer�ncia da sess�o da conex�o com o cliente da solicita��o.
	 */

	private void requestAccountData(FileDescriptor fd)
	{
		AccountDataRequest packet = new AccountDataRequest();
		packet.receive(fd);

		int id = packet.getAccountID();
		Account account = accounts.get(id);

		if (account == null)
			logNotice("conta #%d n�o encontrada (ip: %s).\n", id, fd.getAddressString());
		else
			client.sendAccountData(fd, account);
	}

	/**
	 * Solicita��o para obter informa��es de uma conta especificada pelo seu ID.
	 * Recebe um pacote que ter� alguns dados b�sicos recebidos do servidor.
	 * @param fd refer�ncia da sess�o da conex�o com o cliente da solicita��o.
	 */

	private void requestAccountInfo(FileDescriptor fd)
	{
		AccountInfoRequest packet = new AccountInfoRequest();

		int id = packet.getAccountID();
		Account account = accounts.get(id);
		client.sendAccountInfo(fd, packet, account);
	}

	/**
	 * Solicita��o para alterar o endere�o de e-mail de uma determinada conta no sistema.
	 * Recebe um pacote que tem o ID da conta como endere�o de e-mail atual e novo.
	 * @param fd refer�ncia da sess�o da conex�o com o cliente da solicita��o.
	 */

	private void ackChangeEmail(FileDescriptor fd)
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

	private void updateAccountState(FileDescriptor fd)
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

	private void banAccount(FileDescriptor fd)
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

	// mmo_send_global_accreg(AccountDB* self, int fd, int account_id, int char_id)
	// mmo_save_global_accreg(AccountDB* self, int fd, int account_id, int char_id)
}
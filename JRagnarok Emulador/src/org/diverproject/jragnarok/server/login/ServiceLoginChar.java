package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.minutes;
import static org.diverproject.jragnarok.JRagnarokUtil.now;
import static org.diverproject.jragnarok.JRagnarokUtil.s;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.CLIENT_CHAR_PER_ACCOUNT;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.LOGIN_IP_SYNC_INTERVAL;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.VIP_CHAR_INCREASE;
import static org.diverproject.jragnarok.configs.JRagnarokConfigs.VIP_GROUPID;
import static org.diverproject.jragnarok.packets.request.VipDataRequest.VIP_DATA_VIP;
import static org.diverproject.jragnarok.packets.request.VipDataRequest.VIP_DATA_GM;
import static org.diverproject.jragnarok.packets.request.VipDataRequest.VIP_DATA_FORCE;
import static org.diverproject.jragnarok.packets.request.VipDataRequest.VIP_DATA_NONE;
import static org.diverproject.jragnarok.packets.request.VipDataRequest.VIP_DATA_SHOW_RATES;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logExeception;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logNotice;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.request.AccountDataRequest;
import org.diverproject.jragnarok.packets.request.AccountInfoRequest;
import org.diverproject.jragnarok.packets.request.AuthAccountRequest;
import org.diverproject.jragnarok.packets.request.SendAccountRequest;
import org.diverproject.jragnarok.packets.request.SetAccountOffline;
import org.diverproject.jragnarok.packets.request.SetAccountOnline;
import org.diverproject.jragnarok.packets.request.UpdateUserCount;
import org.diverproject.jragnarok.packets.request.VipDataRequest;
import org.diverproject.jragnarok.packets.response.SyncronizeAddress;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.GroupControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.BitWise8;

/**
 * <h1>Servi�o de Acesso para Servidor de Personagens</h1>
 *
 * <p>Ap�s a identifica��o de uma solicita��o de conex�o com o servidor de acesso e que tenha sido autenticado,
 * caso a solicita��o tenha vindo de um servidor de personagem ser� passado para um analisador de pacotes que
 * ir� despachar redirecionar todas a��es recebidas dos pacotes para este servi�o que ir� operar as a��es.</p>
 *
 * <p>De modo geral todas as solicita��es s�o para que o servidor de acesso busque dados de uma conta.
 * Ap�s buscar esses dados eles s�o armazenados em pacotes e enviados de volta ao servidor de personagem.</p>
 *
 * @see OnlineMap
 * @see ServiceLoginClient
 * @see ServiceLoginServer
 * @see AccountControl
 * @see GroupControl
 * @see AuthAccountMap
 *
 * @author Andrew
 */

public class ServiceLoginChar extends AbstractServiceLogin
{
	/**
	 * Controlador para identificar jogadores online.
	 */
	private OnlineMap onlines;

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
	 * Controle para persist�ncia das contas de jogadores.
	 */
	private GroupControl groups;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthAccountMap auths;

	/**
	 * Cria uma nova inst�ncia do servi�o para gerenciamento de contas.
	 * @param server refer�ncia do servidor de acesso que ir� us�-lo.
	 */

	public ServiceLoginChar(LoginServer server)
	{
		super(server);
	}

	@Override
	public void init()
	{
		client = getServer().getFacade().getClientService();
		login = getServer().getFacade().getLoginService();
		accounts = getServer().getFacade().getAccountControl();
		onlines = getServer().getFacade().getOnlineMap();
		groups = getServer().getFacade().getGroupControl();
		auths = getServer().getFacade().getAuthAccountMap();		

		int interval = getConfigs().getInt(LOGIN_IP_SYNC_INTERVAL);

		if (interval > 0)
		{
			TimerSystem ts = getTimerSystem();
			TimerMap timers = ts.getTimers();

			Timer siaTimer = timers.acquireTimer();
			siaTimer.setListener(SYNCRONIZE_IPADDRESS);
			siaTimer.setTick(ts.getCurrentTime() + minutes(interval));
			ts.getTimers().addLoop(siaTimer, minutes(interval));
		}
	}

	@Override
	public void destroy()
	{
		client = null;
		login = null;
		accounts = null;
		onlines = null;
		groups = null;
		auths = null;
	}

	private final TimerListener SYNCRONIZE_IPADDRESS = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			logInfo("Sincroniza��o de IP em progresso...\n");

			SyncronizeAddress packet = new SyncronizeAddress();
			client.sendAllWithoutOurSelf(null, packet);
		}

		@Override
		public String getName()
		{
			return "syncronizeIpAddress";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	/**
	 * Fun��o para temporizadores executarem a remo��o de uma conta como acesso online.
	 */

	public final TimerListener WAITING_DISCONNECT_TIMER = new TimerListener()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			int accountID = timer.getObjectID();
			OnlineLogin online = onlines.get(accountID);

			onlines.remove(accountID);

			getTimerSystem().getTimers().delete(online.getWaitingDisconnect());
			online.setWaitingDisconnect(null);
		}

		@Override
		public String getName()
		{
			return "waitingDisconnectTimer";
		}

		@Override
		public String toString()
		{
			return getName();
		}
	};

	/**
	 * Um servidor de personagem envia a quantidade de jogadores online.
	 * Deve procurar o cliente desse servidor e atualizar a informa��o.
	 * @param fd conex�o do servidor de personagem que est� enviando.
	 */

	public void updateUserCount(LFileDescriptor fd)
	{
		UpdateUserCount packet = new UpdateUserCount();
		packet.receive(fd);

		ClientCharServer server = getServer().getCharServerList().get(fd);

		if (server != null && server.getUsers() != packet.getCount())
		{
			server.setUsers(s(packet.getCount()));
			logInfo("%d jogadores online em '%s'.\n", server.getUsers(), server.getName());
		}
	}

	/**
	 * Solicita��o para concluir a autentica��o de uma determinada conta j� acessada.
	 * Cada autentica��o pode ser usada uma �nica vez por cada acesso autorizado.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
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
			auths.remove(node); // cada autentica��o � usada uma s� vez
		}

		else
		{
			logInfo("autentica��o de conta RECUSADA (server-fd: %d, ufd: %d).\n", fd.getID(), packet.getFileDescriptorID());
			client.sendAuthAccount(fd, packet);
		}
	}

	/**
	 * Solicita��o para obter dados b�sicos de uma conta especificado pelo seu ID.
	 * Recebe um pacote que ter� alguns dados b�sicos recebidos do servidor.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void requestAccountData(LFileDescriptor fd)
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
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void requestAccountInfo(LFileDescriptor fd)
	{
		AccountInfoRequest packet = new AccountInfoRequest();

		int id = packet.getAccountID();
		Account account = accounts.get(id);
		client.sendAccountInfo(fd, packet, account);
	}

	/**
	 * Solicita��o para enviar ao servidor de personagem os dados VIP de uma conta.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 * @return  true se conseguir enviar ou false caso contr�rio.
	 */

	public boolean requestVipData(LFileDescriptor fd)
	{
		VipDataRequest packet = new VipDataRequest();
		packet.receive(fd);

		Account account = accounts.get(packet.getAccountID());
		BitWise8 flag = new BitWise8(VipDataRequest.VIP_DATA_STRINGS);
		flag.set(packet.getFlag());

		if (account != null)
		{
			int vipGID = getConfigs().getInt(VIP_GROUPID);

			// N�vel de acesso 
			if (account.getGroupID() > vipGID)
			{
				byte flagValue = flag.is(VIP_DATA_GM) || flag.is(VIP_DATA_FORCE) ? VIP_DATA_GM : VIP_DATA_NONE;
				client.sendVipData(fd, account, flagValue, packet.getMapFD());
			}

			long now = now();

			if (flag.is(VIP_DATA_VIP))
			{
				if (account.getGroup().getTime().isNull())
					account.getGroup().getTime().set(now);

				account.getGroup().getTime().set(account.getGroup().getTime().get() + packet.getVipDuration());
			}

			// Conta VIP
			if (now < account.getGroup().getTime().get())
			{
				try {

					if (account.getGroupID() != vipGID)
						account.getGroup().changeCurrentGroup(groups.getGroup(vipGID));

				} catch (RagnarokException e) {
					logError("falha ao alterar o grupo vip (aid: %d, vipGID: %d)", account.getID(), vipGID);
					logExeception(e);
				}

				account.setCharSlots(b(account.getCharSlots() + getConfigs().getInt(VIP_CHAR_INCREASE)));
			}

			// VIP expirou ou foi reduzido
			else
			{
				account.getGroup().getTime().set(0);

				if (account.getGroupID() == vipGID)
					account.getGroup().useOldGroup();

				account.setCharSlots(b(getConfigs().getInt(CLIENT_CHAR_PER_ACCOUNT)));
			}

			accounts.set(account);

			if (flag.is(VIP_DATA_VIP))
			{
				byte vipFlag = !account.getGroup().isVipTimeOver() || flag.is(VIP_DATA_FORCE) ? VIP_DATA_SHOW_RATES : 0;
				client.sendVipData(fd, account, vipFlag, packet.getMapFD());
			}
		}

		return account != null;
	}

	/**
	 * TODO
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void updateCharIP(LFileDescriptor fd)
	{
		// TODO logchrif_parse_updcharip
		
	}

	/**
	 * Recebe uma lista contendo o c�digo de identifica��o de todas as contas que est�o online.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void receiveSentAccounts(LFileDescriptor fd)
	{
		SendAccountRequest packet = new SendAccountRequest();
		packet.receive(fd);

		ClientCharServer server = getServer().getCharServerList().get(fd);
		login.setOfflineUser(server.getID());

		for (int accountID : packet.getAccounts())
			login.addOnlineUser(server.getID(), accountID);
	}

	/**
	 * Recebe uma solicita��o de um servidor de personagem para definir todas as contas como offline.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void setAllOffline(LFileDescriptor fd)
	{
		ClientCharServer server = getServer().getCharServerList().get(fd);

		if (server != null)
			logInfo("%d contas ficaram offline (server: %s)", login.setOfflineUser(server.getID()), server.getName());
	}

	/**
	 * Recebe uma solicita��o de um servidor de personagem para definir uma conta como offline.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void setAccountOffline(LFileDescriptor fd)
	{
		SetAccountOffline packet = new SetAccountOffline();
		packet.receive(fd);

		login.removeOnlineUser(packet.getAccountID());
	}

	/**
	 * Recebe uma solicita��o de um servidor de personagem para definir uma conta como online.
	 * @param fd refer�ncia da sess�o da conex�o com o servidor de personagem.
	 */

	public void setAccountOnline(LFileDescriptor fd)
	{
		SetAccountOnline packet = new SetAccountOnline();
		packet.receive(fd);

		ClientCharServer server = getServer().getCharServerList().get(fd);

		login.addOnlineUser(server.getID(), packet.getAccountID());
	}
}

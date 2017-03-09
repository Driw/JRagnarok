package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.packets.inter.charlogin.HA_VipData.VIP_DATA_FORCE;
import static org.diverproject.jragnarok.packets.inter.charlogin.HA_VipData.VIP_DATA_GM;
import static org.diverproject.jragnarok.packets.inter.charlogin.HA_VipData.VIP_DATA_NONE;
import static org.diverproject.jragnarok.packets.inter.charlogin.HA_VipData.VIP_DATA_SHOW_RATES;
import static org.diverproject.jragnarok.packets.inter.charlogin.HA_VipData.VIP_DATA_VIP;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logException;
import static org.diverproject.log.LogSystem.logInfo;
import static org.diverproject.log.LogSystem.logWarning;
import static org.diverproject.util.Util.b;
import static org.diverproject.util.Util.minutes;
import static org.diverproject.util.Util.now;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.Util.seconds;

import org.diverproject.jragnarok.RagnarokException;
import org.diverproject.jragnarok.packets.common.RefuseLogin;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AccountData;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AccountInfo;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AuthAccount;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_SendAccount;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_SetAccountOffline;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_SetAccountOnline;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_UpdateUserCount;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_VipData;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_SyncronizeAddress;
import org.diverproject.jragnarok.server.Timer;
import org.diverproject.jragnarok.server.TimerAdapt;
import org.diverproject.jragnarok.server.TimerListener;
import org.diverproject.jragnarok.server.TimerMap;
import org.diverproject.jragnarok.server.TimerSystem;
import org.diverproject.jragnarok.server.login.control.AccountControl;
import org.diverproject.jragnarok.server.login.control.GroupControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.BitWise8;

/**
 * <h1>Serviço para Comunicação com o Servidor de Personagem</h1>
 *
 * <p>Após a identificação de uma solicitação de conexão com o servidor de acesso e que tenha sido autenticado,
 * caso a solicitação tenha vindo de um servidor de personagem será passado para um analisador de pacotes que
 * irá despachar redirecionar todas ações recebidas dos pacotes para este serviço que irá operar as ações.</p>
 *
 * <p>De modo geral todas as solicitações são para que o servidor de acesso busque dados de uma conta.
 * Após buscar esses dados eles são armazenados em pacotes e enviados de volta ao servidor de personagem.</p>
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
	 * Controle para persistência das contas de jogadores.
	 */
	private GroupControl groups;

	/**
	 * Controlador para identificar jogadores autenticados.
	 */
	private AuthAccountMap auths;

	/**
	 * Cria uma nova instância do serviço para gerenciamento de contas.
	 * @param server referência do servidor de acesso que irá usá-lo.
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
		groups = getServer().getFacade().getGroupControl();
		auths = getServer().getFacade().getAuthAccountMap();		

		int interval = config().ipSyncInterval;

		TimerSystem ts = getTimerSystem();
		TimerMap timers = ts.getTimers();

		if (interval > 0)
		{
			Timer siaTimer = timers.acquireTimer();
			siaTimer.setListener(SYNCRONIZE_IPADDRESS);
			siaTimer.setTick(ts.getCurrentTime() + minutes(interval));
			ts.getTimers().addLoop(siaTimer, minutes(interval));
		}

		Timer siaTimer = timers.acquireTimer();
		siaTimer.setListener(KEEP_ALIVE);
		siaTimer.setTick(ts.getCurrentTime() + seconds(10));
		ts.getTimers().addLoop(siaTimer, seconds(10));
	}

	@Override
	public void destroy()
	{
		client = null;
		login = null;
		accounts = null;
		groups = null;
		auths = null;
	}

	private final TimerListener SYNCRONIZE_IPADDRESS = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			logInfo("Sincronização de IP em progresso...\n");

			AH_SyncronizeAddress packet = new AH_SyncronizeAddress();
			client.broadcast(null, packet);
		}

		@Override
		public String getName()
		{
			return "SYNCRONIZE_IPADDRESS";
		}
	};

	private final TimerListener KEEP_ALIVE = new TimerAdapt()
	{
		@Override
		public void onCall(Timer timer, int now, int tick)
		{
			CharServerList servers = getServer().getCharServerList();

			for (ClientCharServer server : servers)
				if (!server.getFileDecriptor().isConnected())
					servers.remove(server);
		}

		@Override
		public String getName()
		{
			return "KEEP_ALIVE";
		}
	};

	/**
	 * TODO
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void updateCharIP(LFileDescriptor fd)
	{
		// TODO logchrif_parse_updcharip
		
	}

	/**
	 * Um servidor de personagem envia a quantidade de jogadores online.
	 * Deve procurar o cliente desse servidor e atualizar a informação.
	 * @param fd conexão do servidor de personagem que está enviando.
	 */

	public void updateUserCount(LFileDescriptor fd)
	{
		HA_UpdateUserCount packet = new HA_UpdateUserCount();
		packet.receive(fd);

		ClientCharServer server = getServer().getCharServerList().get(fd);

		if (server != null && server.getUsers() != packet.getCount())
		{
			server.setUsers(s(packet.getCount()));
			logInfo("%d jogadores online em '%s'.\n", server.getUsers(), server.getName());
		}
	}

	/**
	 * Envia um pacote para um servidor de personagem para manter a conexão estabelecida.
	 * Este procedimento é chamado após o servidor de personagem solicitar um ping.
	 * @param fd conexão do descritor de arquivo do servidor de personagem com o servidor de acesso.
	 */

	public void keepAlive(LFileDescriptor fd)
	{
		client.keepAliveCharServer(fd);
	}

	/**
	 * Solicitação para concluir a autenticação de uma determinada conta já acessada.
	 * Cada autenticação pode ser usada uma única vez por cada acesso autorizado.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void requestAuthAccount(LFileDescriptor fd)
	{
		HA_AuthAccount packet = new HA_AuthAccount();
		packet.receive(fd);

		AuthNode node = auths.get(packet.getAccountID());

		if (node != null &&
			node.getAccountID() == packet.getAccountID() &&
			node.getSeed().getFirst() == packet.getFirstSeed() &&
			node.getSeed().getSecond() == packet.getSecondSeed() &&
			node.getSex() == packet.getSex())
		{
			client.sendAuthAccount(fd, packet, node);
			auths.remove(node); // cada autenticação é usada uma só vez
		}

		else
		{
			logInfo("autenticação de conta RECUSADA (server-fd: %d, ufd: %d).\n", fd.getID(), packet.getFileDescriptorID());
			client.refuseLogin(fd, RefuseLogin.RL_REJECTED_FROM_SERVER);
		}
	}

	/**
	 * Solicitação para obter dados básicos de uma conta especificado pelo seu ID.
	 * Recebe um pacote que terá alguns dados básicos recebidos do servidor.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void requestAccountData(LFileDescriptor fd)
	{
		HA_AccountData packet = new HA_AccountData();
		packet.receive(fd);

		int accountID = packet.getAccountID();
		Account account = accounts.get(accountID);

		if (account == null)
			logWarning("dados de conta não encontrada (ip: %s, aid: %d).\n", fd.getAddressString(), accountID);
		else
			client.sendAccountData(fd, packet.getFdID(), account);
	}

	/**
	 * Solicitação para obter informações de uma conta especificada pelo seu ID.
	 * Recebe um pacote que terá alguns dados básicos recebidos do servidor.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void requestAccountInfo(LFileDescriptor fd)
	{
		HA_AccountInfo packet = new HA_AccountInfo();

		int accountID = packet.getAccountID();
		Account account = accounts.get(accountID);

		if (account == null)
			logWarning("informações de conta não encontrada (ip: %s, aid: %d).\n", fd.getAddressString(), accountID);
		else
			client.sendAccountInfo(fd, packet, account);
	}

	/**
	 * Solicitação para enviar ao servidor de personagem os dados VIP de uma conta.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void requestVipData(LFileDescriptor fd)
	{
		HA_VipData packet = new HA_VipData();
		packet.receive(fd);

		int accountID = packet.getAccountID();
		Account account = accounts.get(accountID);

		if (account == null)
			logWarning("informações vip de conta não encontrada (ip: %s, aid: %d).\n", fd.getAddressString(), accountID);

		else
		{
			BitWise8 flag = new BitWise8(HA_VipData.VIP_DATA_STRINGS);
			flag.set(packet.getFlag());

			int vipGID = config().vipGroupID;

			// Nível de acesso 
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
					logException(e);
				}

				account.setCharSlots(b(account.getCharSlots() + config().vipCharIncrease));
			}

			// VIP expirou ou foi reduzido
			else
			{
				account.getGroup().getTime().set(0);

				if (account.getGroupID() == vipGID)
					account.getGroup().useOldGroup();

				account.setCharSlots(b(config().charPerAccount));
			}

			accounts.set(account);

			if (flag.is(VIP_DATA_VIP))
			{
				byte vipFlag = !account.getGroup().isVipTimeOver() || flag.is(VIP_DATA_FORCE) ? VIP_DATA_SHOW_RATES : 0;
				client.sendVipData(fd, account, vipFlag, packet.getMapFD());
			}
		}
	}

	/**
	 * Recebe uma lista contendo o código de identificação de todas as contas que estão online.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void receiveOnlineUsers(LFileDescriptor fd)
	{
		HA_SendAccount packet = new HA_SendAccount();
		packet.receive(fd);

		ClientCharServer server = getServer().getCharServerList().get(fd);
		login.setOfflineUser(server.getID());

		for (int accountID : packet.getAccounts())
			login.addOnlineUser(server.getID(), accountID);
	}

	/**
	 * Recebe uma solicitação de um servidor de personagem para definir todas as contas como offline.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void setAllOffline(LFileDescriptor fd)
	{
		ClientCharServer server = getServer().getCharServerList().get(fd);

		if (server != null)
			logInfo("%d contas ficaram offline (server: %s)", login.setOfflineUser(server.getID()), server.getName());
	}

	/**
	 * Recebe uma solicitação de um servidor de personagem para definir uma conta como offline.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void setAccountOffline(LFileDescriptor fd)
	{
		HA_SetAccountOffline packet = new HA_SetAccountOffline();
		packet.receive(fd);

		login.removeOnlineUser(packet.getAccountID());
	}

	/**
	 * Recebe uma solicitação de um servidor de personagem para definir uma conta como online.
	 * @param fd referência da sessão da conexão com o servidor de personagem.
	 */

	public void setAccountOnline(LFileDescriptor fd)
	{
		HA_SetAccountOnline packet = new HA_SetAccountOnline();
		packet.receive(fd);

		ClientCharServer server = getServer().getCharServerList().get(fd);

		login.addOnlineUser(server.getID(), packet.getAccountID());
	}
}

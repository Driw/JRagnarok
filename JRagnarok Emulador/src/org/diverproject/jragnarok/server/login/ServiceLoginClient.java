package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.i;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Salt;
import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.jragnarok.JRagnarokUtil.random;
import static org.diverproject.jragnarok.JRagnarokUtil.skip;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_KEEP_ALIVE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_UPDATE_CLIENT_HASH;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_HASH;
import static org.diverproject.log.LogSystem.log;
import static org.diverproject.log.LogSystem.logDebug;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.receive.KeepAlive;
import org.diverproject.jragnarok.packets.receive.AcknowledgePacket;
import org.diverproject.jragnarok.packets.receive.UpdateClientHash;
import org.diverproject.jragnarok.packets.request.AccountDataResult;
import org.diverproject.jragnarok.packets.request.AccountInfoRequest;
import org.diverproject.jragnarok.packets.request.AccountInfoResult;
import org.diverproject.jragnarok.packets.request.AccountStateNotify;
import org.diverproject.jragnarok.packets.request.AuthAccountResult;
import org.diverproject.jragnarok.packets.request.CharServerConnectResult;
import org.diverproject.jragnarok.packets.response.AcknowledgeHash;
import org.diverproject.jragnarok.packets.response.ListCharServers;
import org.diverproject.jragnarok.packets.response.NotifyAuth;
import org.diverproject.jragnarok.packets.response.KeepAliveResult;
import org.diverproject.jragnarok.packets.response.RefuseEnter;
import org.diverproject.jragnarok.packets.response.RefuseLoginByte;
import org.diverproject.jragnarok.packets.response.RefuseLoginInt;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.common.AuthResult;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.common.NotifyAuthResult;
import org.diverproject.jragnarok.server.login.control.AuthControl;
import org.diverproject.jragnarok.server.login.control.OnlineControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.Vip;
import org.diverproject.jragnarok.server.login.structures.AuthNode;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.jragnarok.server.login.structures.ClientHash;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;

/**
 * Servi�o para Acesso dos Clientes
 *
 * Esse servi�o � a primeira fronteira do servidor para se comunicar com o cliente.
 * Nele ser� recebido uma nova conex�o solicitada para com o servidor de acesso.
 * Ap�s receber a conex�o dever� analisar e verificar qual o tipo de acesso solicitado.
 *
 * De acordo com o tipo de acesso solicitado, dever� redirecionar a outros servi�os se necess�rio.
 * Fica sendo de sua responsabilidade garantir a autentica��o de qualquer tipo de acesso.
 * Podendo ainda ser necess�rio comunicar-se com outro servi�o para auxili�-lo.
 *
 * @see ServiceLoginServer
 * @see ServiceLoginLog
 * @see ServiceLoginIpBan
 * @see ServiceLoginChar
 * @see OnlineControl
 * @see AuthControl
 *
 * @author Andrew Mello
 */

public class ServiceLoginClient extends AbstractServiceLogin
{
	/**
	 * Cria um novo servi�o de recebimento dos novos clientes com o servidor.
	 * Esse servi�o ir� repassar o cliente para os m�todos corretos conforme pacotes.
	 * @param server refer�ncia do servidor de acesso que deseja criar o servi�o.
	 */

	public ServiceLoginClient(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa o servi�o para recebimento de novos clientes no servidor.
	 */

	public void init()
	{
		log = getServer().getLogService();
		ipban = getServer().getIpBanService();
		auth = getServer().getAuthService();
	}

	/**
	 * Listener usado para receber novas conex�es solicitadas com o servidor de acesso.
	 * A an�lise verifica se a conex�o j� foi feita e se tiver verifica se est� banido.
	 * Caso n�o esteja banido ou n�o haja conex�o estabelece uma nova conex�o.
	 */

	public FileDescriptorListener parse = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			logDebug("parsing fd#%d.\n", fd.getID());

			LFileDescriptor lfd = (LFileDescriptor) fd;

			if (!fd.isConnected())
				return false;

			// J� conectou, verificar se est� banido
			if (lfd.getSessionData().getCache() == null)
				if (!parseBanTime(lfd))
					return true;

			return acknowledgePacket(lfd);
		}
	};

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean acknowledgePacket(LFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd, false);

		short command = packetReceivePacketID.getPacketID();

		switch (command)
		{
			case PACKET_KEEP_ALIVE:
				keepAlive(fd);
				return true;

			case PACKET_UPDATE_CLIENT_HASH:
				updateClientHash(fd);
				return true;

			case PACKET_REQ_HASH:
				parseRequestKey(fd);
				return true;
		}

		return auth.dispatch(command, fd);
	}

	/**
	 * Verifica se o endere�o de IP de uma conex�o foi banida afim de recusar seu acesso.
	 * Essa opera��o s� ter� efeito se tiver sido habilitado o banimento por IP.
	 * @param fd refer�ncia da conex�o do qual deseja verificar o banimento.
	 * @return true se estiver liberado o acesso ou false se estiver banido.
	 */

	private boolean parseBanTime(LFileDescriptor fd)
	{
		if (getConfigs().getBool("ipban.enabled") && ipban.isBanned(fd.getAddress()))
		{
			log("conex�o recusada, ip n�o autorizado (ip: %s).\n", fd.getAddressString());

			log.add(fd.getAddress(), null, -3, "ip banned");
			skip(fd, false, 23);

			RefuseLoginByte refuseLoginPacket = new RefuseLoginByte();
			refuseLoginPacket.setResult(AuthResult.REJECTED_FROM_SERVER);
			refuseLoginPacket.setBlockDate("");
			refuseLoginPacket.send(fd);

			fd.close();

			return false;
		}

		return true;
	}

	/**
	 * Envia um pacote para manter a conex�o com o jogador, assim � poss�vel evitar timeout.
	 * Quando uma conex�o para de transmitir ou receber dados ir� dar timeout no mesmo.
	 * Se a conex�o chegar em timeout significa que o mesmo dever� ser fechado.
	 * @param fd refer�ncia do objeto contendo a conex�o do cliente.
	 */

	public void keepAlive(LFileDescriptor fd)
	{
		KeepAlive keepAlivePacket = new KeepAlive();
		keepAlivePacket.receive(fd, false);

		logDebug("keep alive received fd#%d.\n", fd.getID());
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void updateClientHash(LFileDescriptor fd)
	{
		UpdateClientHash updateClientHashPacket = new UpdateClientHash();
		updateClientHashPacket.receive(fd, false);

		LoginSessionData sd = fd.getSessionData();
		sd.setClientHash(new ClientHash());
		sd.getClientHash().set(updateClientHashPacket.getHashValue());

		logDebug("update client hash received fd#%d.\n", fd.getID());
	}

	/**
	 * Envia o resultado de uma autentica��o de conex�o com o servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da autentica��o solicitada pelo cliente.
	 */

	public void sendAuthResult(LFileDescriptor fd, AuthResult result)
	{
		RefuseLoginByte refuseLoginPacket = new RefuseLoginByte();
		refuseLoginPacket.setResult(result);
		refuseLoginPacket.send(fd);

		logDebug("refuse login byte sent fd#%d.\n", fd.getID());
	}

	/**
	 * Notifica o cliente que houve algum problema ap�s a autentica��o do acesso.
	 * O acesso foi autenticado por�m houve algum problema em liberar o acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da libera��o do acesso para o cliente.
	 */

	public void sendNotifyResult(LFileDescriptor fd, NotifyAuthResult result)
	{
		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);

		logDebug("notify result sent fd#%d.\n", fd.getID());
	}

	/**
	 * Envia o mesmo pacote para todos do servidor exceto a si mesmo.
	 * Caso nenhum cliente seja definido ser� enviados a todos sem exce��o.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param packet refer�ncia do pacote contendo os dados a serem enviados.
	 * @return quantidade de clientes que tiverem os dados recebidos.
	 */

	public int sendAllWithoutOurSelf(LFileDescriptor fd, IResponsePacket packet)
	{
		int count = 0;

		for (ClientCharServer server : getServer().getCharServerList())
			if (fd == null || server.getFileDecriptor().getID() != fd.getID())
			{
				packet.send(server.getFileDecriptor());
				count++;
			}

		logDebug("%d sessions receive %s.\n", count, nameOf(packet));

		return count;
	}

	/**
	 * Analisa uma solicita��o de um cliente para gerar uma chave de acesso com o servidor.
	 * Deve gerar a chave e enviar o mesmo para o cliente possuir a chave de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void parseRequestKey(LFileDescriptor fd)
	{
		short md5KeyLength = (short) (12 + (random() % 4));
		String md5Key = md5Salt(md5KeyLength);

		LoginSessionData sd = fd.getSessionData();
		sd.setMd5Key(md5Key);
		sd.setMd5KeyLenght(md5KeyLength);

		AcknowledgeHash packet = new AcknowledgeHash();
		packet.setMD5KeyLength(md5KeyLength);
		packet.setMD5Key(md5Key);
		packet.send(fd);

		logDebug("md5 key sent fd#%d.\n", fd.getID());
	}

	/**
	 * Envia ao cliente uma lista contendo os dados dos servidores de personagens.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void sendCharServerList(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();
		CharServerList servers = getServer().getCharServerList();

		ListCharServers packet = new ListCharServers();
		packet.setServers(servers);
		packet.setSessionData(sd);
		packet.send(fd);

		logDebug("char-server list sent fd#%d.\n", fd.getID());
	}

	/**
	 * Notifica o cliente de que sua solicita��o de acesso foi recusada pelo servidor.
	 * Nesta notifica��o � permitido definir at� quando o jogador ser� recusado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicita��o do acesso com o servidor.
	 * @param blockDate at� quando o jogador est� sendo bloqueado (20b).
	 */

	public void refuseLogin(LFileDescriptor fd, AuthResult result, String blockDate)
	{
		LoginSessionData sd = fd.getSessionData();

		if (sd.getVersion() >= dateToVersion(20120000))
		{
			RefuseLoginInt packet = new RefuseLoginInt();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(fd);

			logDebug("refuse login int sent fd#%d.\n", fd.getID());
		}

		else
		{
			RefuseLoginByte packet = new RefuseLoginByte();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(fd);

			logDebug("refuse login byte sent fd#%d.\n", fd.getID());
		}
	}

	/**
	 * Recusa a entrada de uma determinada sess�o no servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado que ser� mostrado ao cliente.
	 */

	public void refuseEnter(LFileDescriptor fd, byte result)
	{
		RefuseEnter packet = new RefuseEnter();
		packet.setResult(result);
		packet.send(fd);

		logDebug("refuse enter sent fd#%d.\n", fd.getID());
	}

	/**
	 * Notifica um conex�o com um servidor de personagem o resultado do seu acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicita��o de acesso da conex�o acima.
	 */

	public void charServerResult(LFileDescriptor fd, AuthResult result)
	{
		CharServerConnectResult packet = new CharServerConnectResult();
		packet.setResult(result);
		packet.send(fd);

		logDebug("char-server connect result sent fd#%d.\n", fd.getID());
	}

	/**
	 * Envia um pacote para uma conex�o afim de mant�-la viva no sistema.
	 * Esse pacote � enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void pingCharRequest(LFileDescriptor fd)
	{
		KeepAliveResult packet = new KeepAliveResult();
		packet.send(fd);

		logDebug("keep alive char-server sent fd#%d.\n", fd.getID());
	}

	/**
	 * Responde ao servidor de personagem que a conta possui os dados autenticados.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param node n� contendo as informa��es do acesso autenticado.
	 * @param fdID c�digo de identifica��o da sess�o do servidor de personagens.
	 * @param ok true se tiver sido autenticado ou false caso contr�rio.
	 */

	public void authAccount(LFileDescriptor fd, AuthNode node, int fdID, boolean ok)
	{
		AuthAccountResult response = new AuthAccountResult();
		response.setAccountID(node.getAccountID());
		response.setFirstSeed(node.getSeed().getFirst());
		response.setSecondSeed(node.getSeed().getSecond());
		response.setRequestID(fdID);

		if (ok)
		{
			response.setResult(AuthAccountResult.OK);
			response.setVersion(node.getVersion());
			response.setClientType(node.getClientType());
		}

		else
		{
			response.setResult(AuthAccountResult.FAILED);
			response.setVersion(0);
			response.setClientType(ClientType.CT_NONE);
		}

		logDebug("auth account sent fd#%d.\n", fd.getID());
	}

	/**
	 * Envia os dados de uma conta solicitada por um determinado servidor de personagem.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account conta do qual ter� os dados enviados ao servidor acima.
	 */

	public void sendAccountData(LFileDescriptor fd, Account account)
	{
		AccountDataResult packet = new AccountDataResult();
		packet.setAccountID(account.getID());
		packet.setEmail(account.getEmail());
		packet.setExpirationTime(i(account.getExpiration().get()));
		packet.setGroupID(b(account.getGroup().getID()));
		packet.setCharSlots(account.getCharSlots());
		packet.setBirthdate(account.getBirthDate());

		if (account.getPincode() != null)
		{
			packet.setPincode(account.getPincode().getCode());
			packet.setPincodeChage(i(account.getPincode().getChanged().get()));
		}

		else
		{
			packet.setPincode("0000");
			packet.setPincodeChage(0);
		}

		Vip vip = account.getGroup().getVip();

		if (vip != null)
		{
			packet.setVip(true);
			packet.setCharVip(vip.getCharSlotCount());
			packet.setCharBilling(vip.getCharSlotCount());
		}

		else
		{
			packet.setVip(false);
			packet.setCharVip(b(0));
			packet.setCharBilling(b(0));
		}

		packet.send(fd);

		logDebug("account data sent fd#%d.\n", fd.getID());
	}

	public void sendAccountInfo(LFileDescriptor fd, AccountInfoRequest ack, Account account)
	{
		AccountInfoResult packet = new AccountInfoResult();
		packet.setMapFD(ack.getMapFD());
		packet.setUFD(packet.getUFD());
		packet.setAID(packet.getAID());
		packet.setAccountID(account.getID());
		packet.setData(account != null);

		if (account != null)
		{
			packet.setGroupID(account.getGroup().getID());
			packet.setLoginCount(account.getLoginCount());
			packet.setState(account.getState().CODE);
			packet.setEmail(account.getEmail());
			packet.setLastIP(account.getLastIP().get());
			packet.setLastLogin(i(account.getLastLogin().get()));
			packet.setBirthdate(account.getBirthDate());

			if (ack.getGroupID() >= account.getGroup().getID())
			{
				packet.setPassword(account.getPassword());
				packet.setPincode(account.getPincode().getCode());
			}

			packet.setUsername(account.getUsername());
		}

		logDebug("account info sent fd#%d.\n", fd.getID());
	}

	/**
	 * Envia a todas as conex�es estabelecidas que uma conta teve seu estado alterado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account refer�ncia da conta do qual est� sendo alterada no sistema.
	 * @param banned true se tiver sendo banida ou false se for outro estado.
	 */

	public void sendNotifyAccountState(LFileDescriptor fd, Account account, boolean banned)
	{
		AccountStateNotify notify = new AccountStateNotify();
		notify.setAccountID(account.getID());
		notify.setValue(banned ? i(account.getUnban().get()) : account.getState().CODE);
		notify.setType(banned ? AccountStateNotify.BAN : AccountStateNotify.CHANGE_STATE);

		sendAllWithoutOurSelf(null, notify);		
	}
}

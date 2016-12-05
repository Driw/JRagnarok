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
 * Serviço para Acesso dos Clientes
 *
 * Esse serviço é a primeira fronteira do servidor para se comunicar com o cliente.
 * Nele será recebido uma nova conexão solicitada para com o servidor de acesso.
 * Após receber a conexão deverá analisar e verificar qual o tipo de acesso solicitado.
 *
 * De acordo com o tipo de acesso solicitado, deverá redirecionar a outros serviços se necessário.
 * Fica sendo de sua responsabilidade garantir a autenticação de qualquer tipo de acesso.
 * Podendo ainda ser necessário comunicar-se com outro serviço para auxiliá-lo.
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
	 * Cria um novo serviço de recebimento dos novos clientes com o servidor.
	 * Esse serviço irá repassar o cliente para os métodos corretos conforme pacotes.
	 * @param server referência do servidor de acesso que deseja criar o serviço.
	 */

	public ServiceLoginClient(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa o serviço para recebimento de novos clientes no servidor.
	 */

	public void init()
	{
		log = getServer().getLogService();
		ipban = getServer().getIpBanService();
		auth = getServer().getAuthService();
	}

	/**
	 * Listener usado para receber novas conexões solicitadas com o servidor de acesso.
	 * A análise verifica se a conexão já foi feita e se tiver verifica se está banido.
	 * Caso não esteja banido ou não haja conexão estabelece uma nova conexão.
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

			// Já conectou, verificar se está banido
			if (lfd.getSessionData().getCache() == null)
				if (!parseBanTime(lfd))
					return true;

			return acknowledgePacket(lfd);
		}
	};

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despachá-lo.
	 * @param fd referência da conexão com o cliente para enviar e receber dados.
	 * @return true se o pacote recebido for de um tipo válido para análise.
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
	 * Verifica se o endereço de IP de uma conexão foi banida afim de recusar seu acesso.
	 * Essa operação só terá efeito se tiver sido habilitado o banimento por IP.
	 * @param fd referência da conexão do qual deseja verificar o banimento.
	 * @return true se estiver liberado o acesso ou false se estiver banido.
	 */

	private boolean parseBanTime(LFileDescriptor fd)
	{
		if (getConfigs().getBool("ipban.enabled") && ipban.isBanned(fd.getAddress()))
		{
			log("conexão recusada, ip não autorizado (ip: %s).\n", fd.getAddressString());

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
	 * Envia um pacote para manter a conexão com o jogador, assim é possível evitar timeout.
	 * Quando uma conexão para de transmitir ou receber dados irá dar timeout no mesmo.
	 * Se a conexão chegar em timeout significa que o mesmo deverá ser fechado.
	 * @param fd referência do objeto contendo a conexão do cliente.
	 */

	public void keepAlive(LFileDescriptor fd)
	{
		KeepAlive keepAlivePacket = new KeepAlive();
		keepAlivePacket.receive(fd, false);

		logDebug("keep alive received fd#%d.\n", fd.getID());
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * Envia o resultado de uma autenticação de conexão com o servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da autenticação solicitada pelo cliente.
	 */

	public void sendAuthResult(LFileDescriptor fd, AuthResult result)
	{
		RefuseLoginByte refuseLoginPacket = new RefuseLoginByte();
		refuseLoginPacket.setResult(result);
		refuseLoginPacket.send(fd);

		logDebug("refuse login byte sent fd#%d.\n", fd.getID());
	}

	/**
	 * Notifica o cliente que houve algum problema após a autenticação do acesso.
	 * O acesso foi autenticado porém houve algum problema em liberar o acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da liberação do acesso para o cliente.
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
	 * Caso nenhum cliente seja definido será enviados a todos sem exceção.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param packet referência do pacote contendo os dados a serem enviados.
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
	 * Analisa uma solicitação de um cliente para gerar uma chave de acesso com o servidor.
	 * Deve gerar a chave e enviar o mesmo para o cliente possuir a chave de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * Notifica o cliente de que sua solicitação de acesso foi recusada pelo servidor.
	 * Nesta notificação é permitido definir até quando o jogador será recusado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicitação do acesso com o servidor.
	 * @param blockDate até quando o jogador está sendo bloqueado (20b).
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
	 * Recusa a entrada de uma determinada sessão no servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado que será mostrado ao cliente.
	 */

	public void refuseEnter(LFileDescriptor fd, byte result)
	{
		RefuseEnter packet = new RefuseEnter();
		packet.setResult(result);
		packet.send(fd);

		logDebug("refuse enter sent fd#%d.\n", fd.getID());
	}

	/**
	 * Notifica um conexão com um servidor de personagem o resultado do seu acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicitação de acesso da conexão acima.
	 */

	public void charServerResult(LFileDescriptor fd, AuthResult result)
	{
		CharServerConnectResult packet = new CharServerConnectResult();
		packet.setResult(result);
		packet.send(fd);

		logDebug("char-server connect result sent fd#%d.\n", fd.getID());
	}

	/**
	 * Envia um pacote para uma conexão afim de mantê-la viva no sistema.
	 * Esse pacote é enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void pingCharRequest(LFileDescriptor fd)
	{
		KeepAliveResult packet = new KeepAliveResult();
		packet.send(fd);

		logDebug("keep alive char-server sent fd#%d.\n", fd.getID());
	}

	/**
	 * Responde ao servidor de personagem que a conta possui os dados autenticados.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param node nó contendo as informações do acesso autenticado.
	 * @param fdID código de identificação da sessão do servidor de personagens.
	 * @param ok true se tiver sido autenticado ou false caso contrário.
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
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account conta do qual terá os dados enviados ao servidor acima.
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
	 * Envia a todas as conexões estabelecidas que uma conta teve seu estado alterado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account referência da conta do qual está sendo alterada no sistema.
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

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
import org.diverproject.jragnarok.packets.request.AuthAccountRequest;
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
		super.init();

		log = getServer().getFacade().getLogService();
		ipban = getServer().getFacade().getIpBanService();
		auth = getServer().getFacade().getAuthService();
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
			logDebug("recebendo pacote (fd: %d).\n", fd.getID());

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
		logDebug("ping recebido (fd: %d).\n", fd.getID());

		KeepAlive keepAlivePacket = new KeepAlive();
		keepAlivePacket.receive(fd, false);
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void updateClientHash(LFileDescriptor fd)
	{
		logDebug("atualização para client hash recebido (fd: %d).\n", fd.getID());

		UpdateClientHash updateClientHashPacket = new UpdateClientHash();
		updateClientHashPacket.receive(fd, false);

		LoginSessionData sd = fd.getSessionData();
		sd.setClientHash(new ClientHash());
		sd.getClientHash().set(updateClientHashPacket.getHashValue());
	}

	/**
	 * Envia o resultado de uma autenticação de conexão com o servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da autenticação solicitada pelo cliente.
	 */

	public void sendAuthResult(LFileDescriptor fd, AuthResult result)
	{
		logDebug("enviando resultado de autenticação (fd: %d, result: %s).\n", fd.getID(), result);

		RefuseLoginByte refuseLoginPacket = new RefuseLoginByte();
		refuseLoginPacket.setResult(result);
		refuseLoginPacket.send(fd);
	}

	/**
	 * Notifica o cliente que houve algum problema após a autenticação do acesso.
	 * O acesso foi autenticado porém houve algum problema em liberar o acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da liberação do acesso para o cliente.
	 */

	public void sendNotifyResult(LFileDescriptor fd, NotifyAuthResult result)
	{
		logDebug("notificando autenticação (fd: %d, result: %s).\n", fd.getID(), result);

		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);
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

		logDebug("%d sessões receberam '%s'.\n", count, nameOf(packet));

		return count;
	}

	/**
	 * Analisa uma solicitação de um cliente para gerar uma chave de acesso com o servidor.
	 * Deve gerar a chave e enviar o mesmo para o cliente possuir a chave de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void parseRequestKey(LFileDescriptor fd)
	{
		logDebug("enviando chave md5 (fd: %d).\n", fd.getID());

		short md5KeyLength = (short) (12 + (random() % 4));
		String md5Key = md5Salt(md5KeyLength);

		LoginSessionData sd = fd.getSessionData();
		sd.setMd5Key(md5Key);
		sd.setMd5KeyLenght(md5KeyLength);

		AcknowledgeHash packet = new AcknowledgeHash();
		packet.setMD5KeyLength(md5KeyLength);
		packet.setMD5Key(md5Key);
		packet.send(fd);
	}

	/**
	 * Envia ao cliente uma lista contendo os dados dos servidores de personagens.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void sendCharServerList(LFileDescriptor fd)
	{
		logDebug("lista com servidores de personagem enviado (fd: %d).\n", fd.getID());

		LoginSessionData sd = fd.getSessionData();
		CharServerList servers = getServer().getCharServerList();

		ListCharServers packet = new ListCharServers();
		packet.setServers(servers);
		packet.setSessionData(sd);
		packet.send(fd);
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

		logDebug("acesso recusado (fd: %d, user: %s, result: %s).\n", fd.getID(), sd.getUsername(), result);

		if (sd.getVersion() >= dateToVersion(20120000))
		{
			RefuseLoginInt packet = new RefuseLoginInt();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(fd);
		}

		else
		{
			RefuseLoginByte packet = new RefuseLoginByte();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(fd);
		}
	}

	/**
	 * Recusa a entrada de uma determinada sessão no servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado que será mostrado ao cliente.
	 */

	public void refuseEnter(LFileDescriptor fd, byte result)
	{
		LoginSessionData sd = fd.getSessionData();

		logDebug("entrada recusada (fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

		RefuseEnter packet = new RefuseEnter();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Notifica um conexão com um servidor de personagem o resultado do seu acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicitação de acesso da conexão acima.
	 */

	public void charServerResult(LFileDescriptor fd, AuthResult result)
	{
		LoginSessionData sd = fd.getSessionData();

		logDebug("servidor de personagem conectado (server-fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

		CharServerConnectResult packet = new CharServerConnectResult();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia um pacote para uma conexão afim de mantê-la viva no sistema.
	 * Esse pacote é enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void pingCharRequest(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();

		logDebug("pingar servidor de personagem (server-fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

		KeepAliveResult packet = new KeepAliveResult();
		packet.send(fd);
	}

	/**
	 * Responde ao servidor de personagem que a conta solicitada possui autenticação.
	 * Os dados para a autenticação serão enviados ao servidor de personagem.
	 * @param fd código de identificação da conexão do servidor de personagem com o sistema.
	 * @param packet pacote contendo as informações para serem retornadas já que não autenticação.
	 * @param node nó contendo as informações para autenticação do cliente no servidor de personagem.
	 */

	public void authAccount(LFileDescriptor fd, AuthAccountRequest packet, AuthNode node)
	{
		logDebug("enviando autenticação de conta (server-fd: %d, aid: %s).\n", fd.getID(), node.getAccountID());

		AuthAccountResult response = new AuthAccountResult();
		response.setFileDescriptorID(packet.getFileDescriptorID());
		response.setAccountID(node.getAccountID());
		response.setFirstSeed(node.getSeed().getFirst());
		response.setSecondSeed(node.getSeed().getSecond());
		response.setVersion(node.getVersion());
		response.setClientType(node.getClientType());
		response.setVersion(node.getVersion());
		response.setClientType(node.getClientType());
		response.send(fd);
	}

	/**
	 * Responde ao servidor de personagem que a conta solicitada para autenticar não foi encontrado.
	 * @param fd código de identificação da conexão do servidor de personagem com o sistema.
	 * @param packet pacote contendo as informações para serem retornadas já que não autenticação.
	 */

	public void authAccount(LFileDescriptor fd, AuthAccountRequest packet)
	{
		logDebug("enviando autenticação de conta não encontrada (server-fd: %d).\n", fd.getID());

		AuthAccountResult response = new AuthAccountResult();
		response.setFileDescriptorID(packet.getFileDescriptorID());
		response.setAccountID(packet.getAccountID());
		response.setFirstSeed(packet.getFirstSeed());
		response.setSecondSeed(packet.getSecondSeed());
		response.setVersion(0);
		response.setClientType(ClientType.CT_NONE);
		response.send(fd);
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

		logDebug("enviando dados de uma conta (server-fd: %d, username: %s).\n", fd.getID(), account.getUsername());

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
	}

	/**
	 * Envia a um servidor de personagem o resultado da solicitação das informações de uma conta.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param ack pacote contendo as informações da solicitação que foi desejada.
	 * @param account conta respectiva a solicitação desejada.
	 */

	public void sendAccountInfo(LFileDescriptor fd, AccountInfoRequest ack, Account account)
	{
		AccountInfoResult packet = new AccountInfoResult();
		packet.setMapFD(ack.getServerFD());
		packet.setUFD(packet.getUFD());
		packet.setAccountID(account.getID());
		packet.setData(account != null);

		if (account != null)
		{
			logDebug("enviando informações de uma conta (server-fd: %d, username: %s).\n", fd.getID(), account.getUsername());

			packet.setGroupID(account.getGroup().getID());
			packet.setLoginCount(account.getLoginCount());
			packet.setState(account.getState().CODE);
			packet.setEmail(account.getEmail());
			packet.setLastIP(account.getLastIP().get());
			packet.setLastLogin(i(account.getLastLogin().get()));
			packet.setBirthdate(account.getBirthDate());
			packet.setPassword(account.getPassword());
			packet.setPincode(account.getPincode().getCode());
			packet.setUsername(account.getUsername());
		}
	}

	/**
	 * Envia a todas as conexões estabelecidas que uma conta teve seu estado alterado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account referência da conta do qual está sendo alterada no sistema.
	 * @param banned true se tiver sendo banida ou false se for outro estado.
	 */

	public void sendNotifyAccountState(LFileDescriptor fd, Account account, boolean banned)
	{
		logDebug("notificando alteração de estado (fd: %d, username: %s).\n", fd.getID(), account.getUsername());

		AccountStateNotify notify = new AccountStateNotify();
		notify.setAccountID(account.getID());
		notify.setValue(banned ? i(account.getUnban().get()) : account.getState().CODE);
		notify.setType(banned ? AccountStateNotify.BAN : AccountStateNotify.CHANGE_STATE);

		sendAllWithoutOurSelf(null, notify);		
	}
}

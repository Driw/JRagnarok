package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Salt;
import static org.diverproject.jragnarok.JRagnarokUtil.random;
import static org.diverproject.jragnarok.JRagnarokUtil.skip;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_KEEP_ALIVE;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_UPDATE_CLIENT_HASH;
import static org.diverproject.jragnarok.packets.RagnarokPacketList.PACKET_REQ_HASH;
import static org.diverproject.log.LogSystem.log;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.packets.ResponsePacket;
import org.diverproject.jragnarok.packets.receive.KeepAlive;
import org.diverproject.jragnarok.packets.receive.AcknowledgePacket;
import org.diverproject.jragnarok.packets.receive.UpdateClientHash;
import org.diverproject.jragnarok.packets.response.AcknowledgeHash;
import org.diverproject.jragnarok.packets.response.CharConnectResult;
import org.diverproject.jragnarok.packets.response.ListCharServers;
import org.diverproject.jragnarok.packets.response.NotifyAuth;
import org.diverproject.jragnarok.packets.response.PingResponse;
import org.diverproject.jragnarok.packets.response.RefuseLoginByte;
import org.diverproject.jragnarok.packets.response.RefuseLoginInt;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.login.controllers.AuthControl;
import org.diverproject.jragnarok.server.login.controllers.OnlineControl;
import org.diverproject.jragnarok.server.login.structures.AuthResult;
import org.diverproject.jragnarok.server.login.structures.ClientCharServer;
import org.diverproject.jragnarok.server.login.structures.ClientHash;
import org.diverproject.jragnarok.server.login.structures.LoginSessionData;
import org.diverproject.jragnarok.server.login.structures.NotifyAuthResult;

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
	 * Serviço para registro dos acessos.
	 */
	private ServiceLoginLog log;

	/**
	 * Serviço para banimento por endereço de IP.
	 */
	private ServiceLoginIpBan ipban;

	/**
	 * Serviço para autenticação de acessos.
	 */
	private ServiceLoginAuth auth;

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
			if (!fd.isConnected())
				return false;

			// Já conectou, verificar se está banido
			if (fd.getCache() == null)
				if (!parseBanTime(fd))
					return true;

			return acknowledgePacket(fd);
		}
	};

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despachá-lo.
	 * @param fd referência da conexão com o cliente para enviar e receber dados.
	 * @return true se o pacote recebido for de um tipo válido para análise.
	 */

	private boolean acknowledgePacket(FileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd, false);

		short command = packetReceivePacketID.getPacketID();
		LoginSessionData sd = new LoginSessionData(fd);

		switch (command)
		{
			case PACKET_KEEP_ALIVE:
				keepAlive(fd);
				break;

			case PACKET_UPDATE_CLIENT_HASH:
				updateClientHash(fd, sd);
				break;

			case PACKET_REQ_HASH:
				parseRequestKey(fd, sd);
				break;
		}

		return auth.dispatch(command, fd, sd);
	}

	/**
	 * Verifica se o endereço de IP de uma conexão foi banida afim de recusar seu acesso.
	 * Essa operação só terá efeito se tiver sido habilitado o banimento por IP.
	 * @param fd referência da conexão do qual deseja verificar o banimento.
	 * @return true se estiver liberado o acesso ou false se estiver banido.
	 */

	private boolean parseBanTime(FileDescriptor fd)
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

	public void keepAlive(FileDescriptor fd)
	{
		KeepAlive keepAlivePacket = new KeepAlive();
		keepAlivePacket.receive(fd, false);
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd referência da conexão com o cliente que será recebido.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 */

	public void updateClientHash(FileDescriptor fd, LoginSessionData sd)
	{
		UpdateClientHash updateClientHashPacket = new UpdateClientHash();
		updateClientHashPacket.receive(fd, false);

		sd.setClientHash(new ClientHash());
		sd.getClientHash().set(updateClientHashPacket.getHashValue());
	}

	/**
	 * Envia o resultado de uma autenticação de conexão com o servidor de acesso.
	 * @param fd referência da conexão com o cliente que será enviado.
	 * @param result resultado da autenticação solicitada pelo cliente.
	 */

	public void sendAuthResult(FileDescriptor fd, AuthResult result)
	{
		RefuseLoginByte refuseLoginPacket = new RefuseLoginByte();
		refuseLoginPacket.setResult(result);
		refuseLoginPacket.send(fd);
	}

	/**
	 * Notifica o cliente que houve algum problema após a autenticação do acesso.
	 * O acesso foi autenticado porém houve algum problema em liberar o acesso.
	 * @param fd referência da conexão com o cliente que será enviado.
	 * @param result resultado da liberação do acesso para o cliente.
	 */

	public void sendNotifyResult(FileDescriptor fd, NotifyAuthResult result)
	{
		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia o mesmo pacote para todos do servidor exceto a si mesmo.
	 * Caso nenhum cliente seja definido será enviados a todos sem exceção.
	 * @param fd referência da conexão com o cliente que não receberá o pacote
	 * @param packet referência do pacote contendo os dados a serem enviados.
	 * @return quantidade de clientes que tiverem os dados recebidos.
	 */

	public int sendAllWithoutOurSelf(FileDescriptor fd, ResponsePacket packet)
	{
		int count = 0;

		for (ClientCharServer server : getServer().getCharServerList())
			if (fd == null || server.getFileDecriptor().getID() != fd.getID())
			{
				packet.send(server.getFileDecriptor());
				count++;
			}

		return count;
	}

	/**
	 * Analisa uma solicitação de um cliente para gerar uma chave de acesso com o servidor.
	 * Deve gerar a chave e enviar o mesmo para o cliente possuir a chave de acesso.
	 * @param fd referência da conexão com o cliente que será enviado.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 */

	public void parseRequestKey(FileDescriptor fd, LoginSessionData sd)
	{
		short md5KeyLength = (short) (12 + (random() % 4));
		String md5Key = md5Salt(md5KeyLength);

		sd.setMd5Key(md5Key);
		sd.setMd5KeyLenght(md5KeyLength);

		AcknowledgeHash packet = new AcknowledgeHash();
		packet.setMD5KeyLength(md5KeyLength);
		packet.setMD5Key(md5Key);
		packet.send(fd);
	}

	/**
	 * Envia ao cliente uma lista contendo os dados dos servidores de personagens.
	 * @param sd sessão sessão contendo os dados de acesso do cliente no servidor.
	 */

	public void sendCharServerList(LoginSessionData sd)
	{
		CharServerList servers = getServer().getCharServerList();

		ListCharServers packet = new ListCharServers();
		packet.setServers(servers);
		packet.setSessionData(sd);
		packet.send(sd.getFileDescriptor());
	}

	/**
	 * Notifica o cliente de que sua solicitação de acesso foi recusada pelo servidor.
	 * Nesta notificação é permitido definir até quando o jogador será recusado.
	 * @param sd sessão correspondente ao cliente que foi recusado pelo servidor.
	 * @param result resultado da solicitação do acesso com o servidor.
	 * @param blockDate até quando o jogador está sendo bloqueado (20b).
	 */

	public void refuseLogin(LoginSessionData sd, AuthResult result, String blockDate)
	{
		if (sd.getVersion() >= dateToVersion(20120000))
		{
			RefuseLoginInt packet = new RefuseLoginInt();
			packet.setBlockDate(blockDate);
			packet.setCode(result);
			packet.send(sd.getFileDescriptor());
		}

		else
		{
			RefuseLoginByte packet = new RefuseLoginByte();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(sd.getFileDescriptor());
		}
	}

	/**
	 * Notifica um conexão com um servidor de personagem o resultado do seu acesso.
	 * @param fd conexão do servidor de personagem com este servidor de acesso.
	 * @param result resultado da solicitação de acesso da conexão acima.
	 */

	public void charServerResult(FileDescriptor fd, AuthResult result)
	{
		CharConnectResult packet = new CharConnectResult();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia um pacote para uma conexão afim de mantê-la viva no sistema.
	 * Esse pacote é enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conexão do servidor de personagem com este servidor de acesso.
	 */

	public void pingCharRequest(FileDescriptor fd)
	{
		PingResponse packet = new PingResponse();
		packet.send(fd);
	}
}

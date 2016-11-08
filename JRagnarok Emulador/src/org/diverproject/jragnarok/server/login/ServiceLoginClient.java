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
	 * Servi�o para registro dos acessos.
	 */
	private ServiceLoginLog log;

	/**
	 * Servi�o para banimento por endere�o de IP.
	 */
	private ServiceLoginIpBan ipban;

	/**
	 * Servi�o para autentica��o de acessos.
	 */
	private ServiceLoginAuth auth;

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
			if (!fd.isConnected())
				return false;

			// J� conectou, verificar se est� banido
			if (fd.getCache() == null)
				if (!parseBanTime(fd))
					return true;

			return acknowledgePacket(fd);
		}
	};

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * @param fd refer�ncia da conex�o com o cliente para enviar e receber dados.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
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
	 * Verifica se o endere�o de IP de uma conex�o foi banida afim de recusar seu acesso.
	 * Essa opera��o s� ter� efeito se tiver sido habilitado o banimento por IP.
	 * @param fd refer�ncia da conex�o do qual deseja verificar o banimento.
	 * @return true se estiver liberado o acesso ou false se estiver banido.
	 */

	private boolean parseBanTime(FileDescriptor fd)
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

	public void keepAlive(FileDescriptor fd)
	{
		KeepAlive keepAlivePacket = new KeepAlive();
		keepAlivePacket.receive(fd, false);
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd refer�ncia da conex�o com o cliente que ser� recebido.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
	 */

	public void updateClientHash(FileDescriptor fd, LoginSessionData sd)
	{
		UpdateClientHash updateClientHashPacket = new UpdateClientHash();
		updateClientHashPacket.receive(fd, false);

		sd.setClientHash(new ClientHash());
		sd.getClientHash().set(updateClientHashPacket.getHashValue());
	}

	/**
	 * Envia o resultado de uma autentica��o de conex�o com o servidor de acesso.
	 * @param fd refer�ncia da conex�o com o cliente que ser� enviado.
	 * @param result resultado da autentica��o solicitada pelo cliente.
	 */

	public void sendAuthResult(FileDescriptor fd, AuthResult result)
	{
		RefuseLoginByte refuseLoginPacket = new RefuseLoginByte();
		refuseLoginPacket.setResult(result);
		refuseLoginPacket.send(fd);
	}

	/**
	 * Notifica o cliente que houve algum problema ap�s a autentica��o do acesso.
	 * O acesso foi autenticado por�m houve algum problema em liberar o acesso.
	 * @param fd refer�ncia da conex�o com o cliente que ser� enviado.
	 * @param result resultado da libera��o do acesso para o cliente.
	 */

	public void sendNotifyResult(FileDescriptor fd, NotifyAuthResult result)
	{
		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia o mesmo pacote para todos do servidor exceto a si mesmo.
	 * Caso nenhum cliente seja definido ser� enviados a todos sem exce��o.
	 * @param fd refer�ncia da conex�o com o cliente que n�o receber� o pacote
	 * @param packet refer�ncia do pacote contendo os dados a serem enviados.
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
	 * Analisa uma solicita��o de um cliente para gerar uma chave de acesso com o servidor.
	 * Deve gerar a chave e enviar o mesmo para o cliente possuir a chave de acesso.
	 * @param fd refer�ncia da conex�o com o cliente que ser� enviado.
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
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
	 * @param sd sess�o sess�o contendo os dados de acesso do cliente no servidor.
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
	 * Notifica o cliente de que sua solicita��o de acesso foi recusada pelo servidor.
	 * Nesta notifica��o � permitido definir at� quando o jogador ser� recusado.
	 * @param sd sess�o correspondente ao cliente que foi recusado pelo servidor.
	 * @param result resultado da solicita��o do acesso com o servidor.
	 * @param blockDate at� quando o jogador est� sendo bloqueado (20b).
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
	 * Notifica um conex�o com um servidor de personagem o resultado do seu acesso.
	 * @param fd conex�o do servidor de personagem com este servidor de acesso.
	 * @param result resultado da solicita��o de acesso da conex�o acima.
	 */

	public void charServerResult(FileDescriptor fd, AuthResult result)
	{
		CharConnectResult packet = new CharConnectResult();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia um pacote para uma conex�o afim de mant�-la viva no sistema.
	 * Esse pacote � enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conex�o do servidor de personagem com este servidor de acesso.
	 */

	public void pingCharRequest(FileDescriptor fd)
	{
		PingResponse packet = new PingResponse();
		packet.send(fd);
	}
}

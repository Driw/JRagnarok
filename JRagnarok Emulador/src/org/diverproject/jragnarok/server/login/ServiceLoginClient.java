package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokUtil.b;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.i;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Salt;
import static org.diverproject.jragnarok.JRagnarokUtil.nameOf;
import static org.diverproject.jragnarok.JRagnarokUtil.random;
import static org.diverproject.log.LogSystem.logDebug;

import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.receive.KeepAlive;
import org.diverproject.jragnarok.packets.receive.UpdateClientHash;
import org.diverproject.jragnarok.packets.request.AccountDataResult;
import org.diverproject.jragnarok.packets.request.AccountInfoRequest;
import org.diverproject.jragnarok.packets.request.AccountInfoResult;
import org.diverproject.jragnarok.packets.request.AccountStateNotify;
import org.diverproject.jragnarok.packets.request.AuthAccountRequest;
import org.diverproject.jragnarok.packets.request.AuthAccountResult;
import org.diverproject.jragnarok.packets.request.CharServerConnectResult;
import org.diverproject.jragnarok.packets.request.VipDataResult;
import org.diverproject.jragnarok.packets.response.AcknowledgeHash;
import org.diverproject.jragnarok.packets.response.ListCharServers;
import org.diverproject.jragnarok.packets.response.NotifyAuth;
import org.diverproject.jragnarok.packets.response.KeepAliveResult;
import org.diverproject.jragnarok.packets.response.RefuseEnter;
import org.diverproject.jragnarok.packets.response.RefuseLoginByte;
import org.diverproject.jragnarok.packets.response.RefuseLoginInt;
import org.diverproject.jragnarok.server.common.AuthResult;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.common.NotifyAuthResult;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.jragnarok.server.login.entities.Vip;

/**
 * <h1>Servi�o para Comunica��o com o Cliente</h1>
 *
 * <p>Atrav�s deste servi�o ser� poss�vel que outros servi�os solicitem o envio de dados para um cliente.
 * Este cliente pode ser tanto um jogador utilizando o cliente execut�vel como um servidor (servidor de personagem).
 * A �nica responsabilidade deste servi�o � conter m�todos que permita enviar dados aos clientes (conex�es).</p>
 *
 * <p>Al�m de enviar dados aos clientes pode possuir alguns m�todos para receber dados dos clientes.
 * Por exemplo o pacote para manter a conex�o viva no sistema que � recebida em keepAlive().
 * Este tipo de pacote tamb�m poder� ser tratado aqui, j� que � uma opera��o entre cliente/servidor direta.</p>
 *
 * <p>Opera��es indiretas que precisem de outras informa��es ou solicita��es de controles n�o s�o tratadas aqui.
 * Todos os m�todos tem como objetivo apenas simplificar a utiliza��o dos pacotes em rela��o a receber ou enviar.</p>
 *
 * @see AbstractServiceLogin
 * @see LFileDescriptor
 * @see LoginServer
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

	@Override
	public void init()
	{
		
	}

	@Override
	public void destroy()
	{
		
	}

	/**
	 * Envia um pacote para manter a conex�o com o jogador, assim � poss�vel evitar timeout.
	 * Quando uma conex�o para de transmitir ou receber dados ir� dar timeout no mesmo.
	 * Se a conex�o chegar em timeout significa que ficou ociosa e dever� ser fechada.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void keepAlive(LFileDescriptor fd)
	{
		logDebug("recebendo ping (fd: %d).\n", fd.getID());

		KeepAlive packet = new KeepAlive();
		packet.receive(fd);
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void updateClientHash(LFileDescriptor fd)
	{
		logDebug("recebendo atualiza��o para o cliente hash (fd: %d).\n", fd.getID());

		UpdateClientHash packet = new UpdateClientHash();
		packet.receive(fd);

		LoginSessionData sd = fd.getSessionData();
		sd.setClientHash(new ClientHash());
		sd.getClientHash().set(packet.getHashValue());
	}

	/**
	 * Envia o resultado de uma autentica��o de conex�o com o servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da autentica��o solicitada pelo cliente.
	 */

	public void sendAuthResult(LFileDescriptor fd, AuthResult result)
	{
		logDebug("enviando resultado de autentica��o (fd: %d, result: %s).\n", fd.getID(), result);

		RefuseLoginByte refuseLoginPacket = new RefuseLoginByte();
		refuseLoginPacket.setResult(result);
		refuseLoginPacket.send(fd);
	}

	/**
	 * Notifica o cliente que houve algum problema ap�s a autentica��o do acesso.
	 * O acesso foi autenticado por�m houve algum problema em liberar o acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da libera��o do acesso para o cliente.
	 */

	public void sendNotifyResult(LFileDescriptor fd, NotifyAuthResult result)
	{
		logDebug("notificando autentica��o (fd: %d, result: %s).\n", fd.getID(), result);

		NotifyAuth packet = new NotifyAuth();
		packet.setResult(result);
		packet.send(fd);
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

		logDebug("%d sess�es receberam '%s'.\n", count, nameOf(packet));

		return count;
	}

	/**
	 * Analisa uma solicita��o de um cliente para gerar uma chave de acesso com o servidor.
	 * Deve gerar a chave e enviar o mesmo para o cliente possuir a chave de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
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
	 * Notifica o cliente de que sua solicita��o de acesso foi recusada pelo servidor.
	 * Nesta notifica��o � permitido definir at� quando o jogador ser� recusado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicita��o do acesso com o servidor.
	 * @param blockDate at� quando o jogador est� sendo bloqueado (20b).
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
	 * Recusa a entrada de uma determinada sess�o no servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado que ser� mostrado ao cliente.
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
	 * Notifica um conex�o com um servidor de personagem o resultado do seu acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicita��o de acesso da conex�o acima.
	 */

	public void sendCharServerResult(LFileDescriptor fd, AuthResult result)
	{
		LoginSessionData sd = fd.getSessionData();

		logDebug("servidor de personagem conectado (server-fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

		CharServerConnectResult packet = new CharServerConnectResult();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia um pacote para uma conex�o afim de mant�-la viva no sistema.
	 * Esse pacote � enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void pingCharRequest(LFileDescriptor fd)
	{
		LoginSessionData sd = fd.getSessionData();

		logDebug("pingar servidor de personagem (server-fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

		KeepAliveResult packet = new KeepAliveResult();
		packet.send(fd);
	}

	/**
	 * Responde ao servidor de personagem que a conta solicitada possui autentica��o.
	 * Os dados para a autentica��o ser�o enviados ao servidor de personagem.
	 * @param fd c�digo de identifica��o da conex�o do servidor de personagem com o sistema.
	 * @param packet pacote contendo as informa��es para serem retornadas j� que n�o autentica��o.
	 * @param node n� contendo as informa��es para autentica��o do cliente no servidor de personagem.
	 */

	public void sendAuthAccount(LFileDescriptor fd, AuthAccountRequest packet, AuthNode node)
	{
		logDebug("enviando autentica��o de conta (server-fd: %d, aid: %s).\n", fd.getID(), node.getAccountID());

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
	 * Responde ao servidor de personagem que a conta solicitada para autenticar n�o foi encontrado.
	 * @param fd c�digo de identifica��o da conex�o do servidor de personagem com o sistema.
	 * @param packet pacote contendo as informa��es para serem retornadas j� que n�o autentica��o.
	 */

	public void sendAuthAccount(LFileDescriptor fd, AuthAccountRequest packet)
	{
		logDebug("enviando autentica��o de conta n�o encontrada (server-fd: %d).\n", fd.getID());

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
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account conta do qual ter� os dados enviados ao servidor acima.
	 */

	public void sendAccountData(LFileDescriptor fd, Account account)
	{
		AccountDataResult packet = new AccountDataResult();
		packet.setAccountID(account.getID());
		packet.setEmail(account.getEmail());
		packet.setExpirationTime(i(account.getExpiration().get()));
		packet.setGroupID(b(account.getGroup().getCurrentGroup().getID()));
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
	 * Envia a um servidor de personagem o resultado da solicita��o das informa��es de uma conta.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param ack pacote contendo as informa��es da solicita��o que foi desejada.
	 * @param account conta respectiva a solicita��o desejada.
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
			logDebug("enviando informa��es de uma conta (server-fd: %d, username: %s).\n", fd.getID(), account.getUsername());

			packet.setGroupID(account.getGroup().getCurrentGroup().getID());
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
	 * Envia a todas as conex�es estabelecidas que uma conta teve seu estado alterado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account refer�ncia da conta do qual est� sendo alterada no sistema.
	 * @param banned true se tiver sendo banida ou false se for outro estado.
	 */

	public void sendNotifyAccountState(LFileDescriptor fd, Account account, boolean banned)
	{
		logDebug("notificando altera��o de estado (fd: %d, username: %s).\n", fd.getID(), account.getUsername());

		AccountStateNotify notify = new AccountStateNotify();
		notify.setAccountID(account.getID());
		notify.setValue(banned ? i(account.getUnban().get()) : account.getState().CODE);
		notify.setType(banned ? AccountStateNotify.BAN : AccountStateNotify.CHANGE_STATE);

		sendAllWithoutOurSelf(null, notify);		
	}

	/**
	 * Envia os dados da conta atualizada conforme a solicita��o dos dados vip de uma conta.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param account conta contendo as informa��es que devem ser enviadas.
	 * @param flag tipo de vip que foi concedido a informa��es da conta.
	 * @param mapFD c�digo da conex�o do servidor de personagem com o servidor de mapa.
	 */

	public void sendVipData(LFileDescriptor fd, Account account, byte flag, int mapFD)
	{
		VipDataResult packet = new VipDataResult();
		packet.setVipTimeout(account.getGroup().getTime().get());
		packet.setAccountID(account.getID());
		packet.setGroupID(account.getGroupID());
		packet.setMapFD(mapFD);
		packet.setFlag(flag);
		// TODO colocar os dados restantes da conta
		packet.send(fd);
	}
}

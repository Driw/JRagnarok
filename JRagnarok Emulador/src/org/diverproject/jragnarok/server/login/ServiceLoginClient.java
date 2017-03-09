package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnarok.JRagnarokConstants.MIN_CHARS;
import static org.diverproject.jragnarok.JRagnarokUtil.dateToVersion;
import static org.diverproject.jragnarok.JRagnarokUtil.md5Salt;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.util.Util.b;
import static org.diverproject.util.Util.i;
import static org.diverproject.util.Util.nameOf;
import static org.diverproject.util.Util.random;

import org.diverproject.jragnarok.packets.IResponsePacket;
import org.diverproject.jragnarok.packets.character.toclient.HC_RefuseEnter;
import org.diverproject.jragnarok.packets.common.NotifyAuth;
import org.diverproject.jragnarok.packets.common.RefuseEnter;
import org.diverproject.jragnarok.packets.common.RefuseLogin;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AccountInfo;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AccountStateNotify;
import org.diverproject.jragnarok.packets.inter.charlogin.HA_AuthAccount;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AccountData;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AccountInfo;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AckConnect;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_AuthAccount;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_GlobalRegisters;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_KeepAlive;
import org.diverproject.jragnarok.packets.inter.loginchar.AH_VipData;
import org.diverproject.jragnarok.packets.inter.SC_NotifyBan;
import org.diverproject.jragnarok.packets.inter.SS_GroupData;
import org.diverproject.jragnarok.packets.login.fromclient.CA_ConnectInfoChanged;
import org.diverproject.jragnarok.packets.login.fromclient.CA_ExeHashCheck;
import org.diverproject.jragnarok.packets.login.toclient.AC_AccepLogin;
import org.diverproject.jragnarok.packets.login.toclient.AC_AckHash;
import org.diverproject.jragnarok.packets.login.toclient.AC_RefuseLogin;
import org.diverproject.jragnarok.packets.login.toclient.AC_RefuseLoginR2;
import org.diverproject.jragnarok.server.common.ClientType;
import org.diverproject.jragnarok.server.common.GlobalRegister;
import org.diverproject.jragnarok.server.common.entities.Vip;
import org.diverproject.jragnarok.server.login.control.GroupControl;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.collection.Queue;

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
		CA_ConnectInfoChanged packet = new CA_ConnectInfoChanged();
		packet.receive(fd);
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void updateClientHash(LFileDescriptor fd)
	{
		logDebug("recebendo client hash (fd: %d).\n", fd.getID());

		CA_ExeHashCheck packet = new CA_ExeHashCheck();
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

	public void refuseLogin(LFileDescriptor fd, RefuseLogin result)
	{
		refuseLogin(fd, result, "");
	}

	/**
	 * Notifica o cliente de que sua solicita��o de acesso foi recusada pelo servidor.
	 * Nesta notifica��o � permitido definir at� quando o jogador ser� recusado.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicita��o do acesso com o servidor.
	 * @param blockDate at� quando o jogador est� sendo bloqueado (20b).
	 */

	public void refuseLogin(LFileDescriptor fd, RefuseLogin result, String blockDate)
	{
		LoginSessionData sd = fd.getSessionData();

		logDebug("acesso recusado (fd: %d, user: %s, result: %s).\n", fd.getID(), sd.getUsername(), result);

		if (sd.getVersion() >= dateToVersion(20120000))
		{
			AC_RefuseLoginR2 packet = new AC_RefuseLoginR2();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(fd);
		}

		else
		{
			AC_RefuseLogin packet = new AC_RefuseLogin();
			packet.setBlockDate(blockDate);
			packet.setResult(result);
			packet.send(fd);
		}
	}

	/**
	 * Notifica o cliente que houve algum problema ap�s a autentica��o do acesso.
	 * O acesso foi autenticado por�m houve algum problema em liberar o acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da libera��o do acesso para o cliente.
	 */

	public void notifyBan(LFileDescriptor fd, NotifyAuth result)
	{
		logDebug("notificando autentica��o (fd: %d, result: %s).\n", fd.getID(), result);

		SC_NotifyBan packet = new SC_NotifyBan();
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

	public int broadcast(LFileDescriptor fd, IResponsePacket packet)
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

	public void parseMD5Key(LFileDescriptor fd)
	{
		logDebug("enviando chave md5 (fd: %d).\n", fd.getID());

		short md5KeyLength = (short) (12 + (random() % 4));
		String md5Key = md5Salt(md5KeyLength);

		LoginSessionData sd = fd.getSessionData();
		sd.setMd5Key(md5Key);
		sd.setMd5KeyLenght(md5KeyLength);

		AC_AckHash packet = new AC_AckHash();
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
		logDebug("enviando lista com servidores de personagem dispon�veis (fd: %d).\n", fd.getID());

		LoginSessionData sd = fd.getSessionData();
		CharServerList servers = getServer().getCharServerList();

		AC_AccepLogin packet = new AC_AccepLogin();
		packet.setServers(servers);
		packet.setSessionData(sd);
		packet.send(fd);
	}

	/**
	 * Recusa a entrada de uma determinada sess�o no servidor de acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param error resultado que ser� mostrado ao cliente.
	 */

	public void refuseEnter(LFileDescriptor fd, RefuseEnter error)
	{
		LoginSessionData sd = fd.getSessionData();

		logDebug("entrada recusada (fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

		HC_RefuseEnter packet = new HC_RefuseEnter();
		packet.setError(error);
		packet.send(fd);
	}

	/**
	 * Notifica um conex�o com um servidor de personagem o resultado do seu acesso.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicita��o de acesso da conex�o acima.
	 */

	public void sendCharServerResult(LFileDescriptor fd, RefuseLogin result)
	{
		LoginSessionData sd = fd.getSessionData();

		logDebug("servidor de personagem conectado (server-fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

		AH_AckConnect packet = new AH_AckConnect();
		packet.setResult(result);
		packet.send(fd);
	}

	/**
	 * Envia um pacote para uma conex�o afim de mant�-la viva no sistema.
	 * Esse pacote � enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 */

	public void keepAliveCharServer(LFileDescriptor fd)
	{
		if (fd.isConnected())
		{
			LoginSessionData sd = fd.getSessionData();

			logDebug("pingar servidor de personagem (server-fd: %d, username: %s).\n", fd.getID(), sd.getUsername());

			AH_KeepAlive packet = new AH_KeepAlive();
			packet.send(fd);
		}
	}

	/**
	 * Responde ao servidor de personagem que a conta solicitada possui autentica��o.
	 * Os dados para a autentica��o ser�o enviados ao servidor de personagem.
	 * @param fd c�digo de identifica��o da conex�o do servidor de personagem com o sistema.
	 * @param packet pacote contendo as informa��es para serem retornadas j� que n�o autentica��o.
	 * @param node n� contendo as informa��es para autentica��o do cliente no servidor de personagem.
	 */

	public void sendAuthAccount(LFileDescriptor fd, HA_AuthAccount packet, AuthNode node)
	{
		logDebug("enviando autentica��o de conta (server-fd: %d, aid: %s).\n", fd.getID(), node.getAccountID());

		AH_AuthAccount response = new AH_AuthAccount();
		response.setFileDescriptorID(packet.getFileDescriptorID());
		response.setAccountID(node.getAccountID());
		response.setFirstSeed(node.getSeed().getFirst());
		response.setSecondSeed(node.getSeed().getSecond());
		response.setVersion(node.getVersion());
		response.setClientType(node.getClientType());
		response.setVersion(node.getVersion());
		response.setClientType(node.getClientType());
		response.setSex(node.getSex());
		response.setResult(true);
		response.send(fd);
	}

	/**
	 * Responde ao servidor de personagem que a conta solicitada para autenticar n�o foi encontrado.
	 * @param fd c�digo de identifica��o da conex�o do servidor de personagem com o sistema.
	 * @param packet pacote contendo as informa��es para serem retornadas j� que n�o autentica��o.
	 */

	public void sendAuthAccount(LFileDescriptor fd, HA_AuthAccount packet)
	{
		logDebug("enviando autentica��o de conta n�o encontrada (server-fd: %d).\n", fd.getID());

		AH_AuthAccount response = new AH_AuthAccount();
		response.setFileDescriptorID(packet.getFileDescriptorID());
		response.setAccountID(packet.getAccountID());
		response.setFirstSeed(packet.getFirstSeed());
		response.setSecondSeed(packet.getSecondSeed());
		response.setVersion(0);
		response.setClientType(ClientType.CT_NONE);
		response.setSex(packet.getSex());
		response.setResult(false);
		response.send(fd);
	}

	/**
	 * Envia os dados de uma conta solicitada por um determinado servidor de personagem.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor.
	 * @param fdID c�digo do descritor de arquivo que solicitou os dados da conta.
	 * @param account conta do qual ter� os dados enviados ao servidor acima.
	 */

	public void sendAccountData(LFileDescriptor fd, int fdID, Account account)
	{
		logDebug("enviando dados de uma conta (server-fd: %d, username: %s).\n", fd.getID(), account.getUsername());

		AH_AccountData packet = new AH_AccountData();
		packet.setFdID(fdID);
		packet.setAccountID(account.getID());
		packet.setEmail(account.getEmail());
		packet.setExpirationTime(i(account.getExpiration().get()));
		packet.setGroupID(b(account.getGroup().getCurrentGroup().getID()));
		packet.setBirthdate(account.getBirthDate());
		packet.setCharSlots(MIN_CHARS);

		packet.setPincodeEnabled(account.getPincode().isEnabled());
		packet.setPincode(account.getPincode().getCode());
		packet.setPincodeChage(account.getPincode().getChanged().get());

		Vip vip = account.getGroup().getVip();
		packet.setVipID(vip == null ? 0 : vip.getID());

		packet.send(fd);
	}

	/**
	 * Envia a um servidor de personagem o resultado da solicita��o das informa��es de uma conta.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param ack pacote contendo as informa��es da solicita��o que foi desejada.
	 * @param account conta respectiva a solicita��o desejada.
	 */

	public void sendAccountInfo(LFileDescriptor fd, HA_AccountInfo ack, Account account)
	{
		AH_AccountInfo packet = new AH_AccountInfo();
		packet.setMapFD(ack.getServerFD());
		packet.setUserFD(packet.getUserFD());
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

	public void sendAccountStateNotify(LFileDescriptor fd, Account account, boolean banned)
	{
		logDebug("notificando altera��o de estado (fd: %d, username: %s).\n", fd.getID(), account.getUsername());

		HA_AccountStateNotify notify = new HA_AccountStateNotify();
		notify.setAccountID(account.getID());
		notify.setValue(banned ? i(account.getUnban().get()) : account.getState().CODE);
		notify.setBanned(banned);

		broadcast(null, notify);		
	}

	/**
	 * Envia os dados da conta atualizada conforme a solicita��o dos dados vip de uma conta.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param account conta contendo as informa��es que devem ser enviadas.
	 * @param flag tipo de vip que foi concedido a informa��es da conta.
	 * @param mapFD c�digo da conex�o do servidor de personagem com o servidor de mapa.
	 */

	public void sendVipData(LFileDescriptor fd, Account account, byte flag, int mapFD)
	{
		AH_VipData packet = new AH_VipData();
		packet.setVipTimeout(account.getGroup().getTime().get());
		packet.setAccountID(account.getID());
		packet.setVipGroupID(account.getGroupID());
		packet.setMapFD(mapFD);
		packet.setVipFlag(flag);
		// TODO colocar os dados restantes da conta
		packet.send(fd);
	}

	/**
	 * Envia a um servidor de personagem o resultado da solicita��o dos registros de vari�veis de uma conta.
	 * Os registros s�o alocados em uma fila que ordena os elementos atrav�s de n�s para agilizar.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param accountID c�digo de identifica��o da conta do qual as vari�veis pertencem.
	 * @param charID c�digo de identifica��o do personagem que ir� receber as vari�veis.
	 * @param registers fila contendo todos os registros encontrados no sistema.
	 */

	public void sendGlobalRegisters(LFileDescriptor fd, int accountID, int charID, Queue<GlobalRegister<?>> registers)
	{
		AH_GlobalRegisters packet = new AH_GlobalRegisters();
		packet.setAccountID(accountID);
		packet.setCharID(charID);
		packet.setRegisters(registers);
		packet.send(fd);
	}

	/**
	 * Repassa todas as informa��es dos grupos de contas e tipos de acesso VIP a um servidor de personagem.
	 * @param fd conex�o do descritor de arquivo do servidor de acesso com o servidor de personagem.
	 * @param groupControl controle de grupos que ir� informar os dados dos grupos e acessos VIP.
	 */

	public void sendGroupData(LFileDescriptor fd, GroupControl groupControl)
	{
		SS_GroupData packet = new SS_GroupData();
		packet.setGroups(groupControl.exportGroups());
		packet.setVips(groupControl.exportVips());
		packet.send(fd);
	}
}

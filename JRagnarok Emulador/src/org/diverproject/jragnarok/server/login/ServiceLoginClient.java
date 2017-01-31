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
 * <h1>Serviço para Comunicação com o Cliente</h1>
 *
 * <p>Através deste serviço será possível que outros serviços solicitem o envio de dados para um cliente.
 * Este cliente pode ser tanto um jogador utilizando o cliente executável como um servidor (servidor de personagem).
 * A única responsabilidade deste serviço é conter métodos que permita enviar dados aos clientes (conexões).</p>
 *
 * <p>Além de enviar dados aos clientes pode possuir alguns métodos para receber dados dos clientes.
 * Por exemplo o pacote para manter a conexão viva no sistema que é recebida em keepAlive().
 * Este tipo de pacote também poderá ser tratado aqui, já que é uma operação entre cliente/servidor direta.</p>
 *
 * <p>Operações indiretas que precisem de outras informações ou solicitações de controles não são tratadas aqui.
 * Todos os métodos tem como objetivo apenas simplificar a utilização dos pacotes em relação a receber ou enviar.</p>
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
	 * Cria um novo serviço de recebimento dos novos clientes com o servidor.
	 * Esse serviço irá repassar o cliente para os métodos corretos conforme pacotes.
	 * @param server referência do servidor de acesso que deseja criar o serviço.
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
	 * Envia um pacote para manter a conexão com o jogador, assim é possível evitar timeout.
	 * Quando uma conexão para de transmitir ou receber dados irá dar timeout no mesmo.
	 * Se a conexão chegar em timeout significa que ficou ociosa e deverá ser fechada.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void keepAlive(LFileDescriptor fd)
	{
		CA_ConnectInfoChanged packet = new CA_ConnectInfoChanged();
		packet.receive(fd);
	}

	/**
	 * Recebe um pacote para atualizar o client hash de um dos clientes conectados.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * Envia o resultado de uma autenticação de conexão com o servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da autenticação solicitada pelo cliente.
	 */

	public void refuseLogin(LFileDescriptor fd, RefuseLogin result)
	{
		refuseLogin(fd, result, "");
	}

	/**
	 * Notifica o cliente de que sua solicitação de acesso foi recusada pelo servidor.
	 * Nesta notificação é permitido definir até quando o jogador será recusado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicitação do acesso com o servidor.
	 * @param blockDate até quando o jogador está sendo bloqueado (20b).
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
	 * Notifica o cliente que houve algum problema após a autenticação do acesso.
	 * O acesso foi autenticado porém houve algum problema em liberar o acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da liberação do acesso para o cliente.
	 */

	public void notifyBan(LFileDescriptor fd, NotifyAuth result)
	{
		logDebug("notificando autenticação (fd: %d, result: %s).\n", fd.getID(), result);

		SC_NotifyBan packet = new SC_NotifyBan();
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

	public int broadcast(LFileDescriptor fd, IResponsePacket packet)
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
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 */

	public void sendCharServerList(LFileDescriptor fd)
	{
		logDebug("enviando lista com servidores de personagem disponíveis (fd: %d).\n", fd.getID());

		LoginSessionData sd = fd.getSessionData();
		CharServerList servers = getServer().getCharServerList();

		AC_AccepLogin packet = new AC_AccepLogin();
		packet.setServers(servers);
		packet.setSessionData(sd);
		packet.send(fd);
	}

	/**
	 * Recusa a entrada de uma determinada sessão no servidor de acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param error resultado que será mostrado ao cliente.
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
	 * Notifica um conexão com um servidor de personagem o resultado do seu acesso.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param result resultado da solicitação de acesso da conexão acima.
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
	 * Envia um pacote para uma conexão afim de mantê-la viva no sistema.
	 * Esse pacote é enviado a um servidor de personagem quando este solicita um ping.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
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
	 * Responde ao servidor de personagem que a conta solicitada possui autenticação.
	 * Os dados para a autenticação serão enviados ao servidor de personagem.
	 * @param fd código de identificação da conexão do servidor de personagem com o sistema.
	 * @param packet pacote contendo as informações para serem retornadas já que não autenticação.
	 * @param node nó contendo as informações para autenticação do cliente no servidor de personagem.
	 */

	public void sendAuthAccount(LFileDescriptor fd, HA_AuthAccount packet, AuthNode node)
	{
		logDebug("enviando autenticação de conta (server-fd: %d, aid: %s).\n", fd.getID(), node.getAccountID());

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
	 * Responde ao servidor de personagem que a conta solicitada para autenticar não foi encontrado.
	 * @param fd código de identificação da conexão do servidor de personagem com o sistema.
	 * @param packet pacote contendo as informações para serem retornadas já que não autenticação.
	 */

	public void sendAuthAccount(LFileDescriptor fd, HA_AuthAccount packet)
	{
		logDebug("enviando autenticação de conta não encontrada (server-fd: %d).\n", fd.getID());

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
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param fdID código do descritor de arquivo que solicitou os dados da conta.
	 * @param account conta do qual terá os dados enviados ao servidor acima.
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
	 * Envia a um servidor de personagem o resultado da solicitação das informações de uma conta.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param ack pacote contendo as informações da solicitação que foi desejada.
	 * @param account conta respectiva a solicitação desejada.
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
			logDebug("enviando informações de uma conta (server-fd: %d, username: %s).\n", fd.getID(), account.getUsername());

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
	 * Envia a todas as conexões estabelecidas que uma conta teve seu estado alterado.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor.
	 * @param account referência da conta do qual está sendo alterada no sistema.
	 * @param banned true se tiver sendo banida ou false se for outro estado.
	 */

	public void sendAccountStateNotify(LFileDescriptor fd, Account account, boolean banned)
	{
		logDebug("notificando alteração de estado (fd: %d, username: %s).\n", fd.getID(), account.getUsername());

		HA_AccountStateNotify notify = new HA_AccountStateNotify();
		notify.setAccountID(account.getID());
		notify.setValue(banned ? i(account.getUnban().get()) : account.getState().CODE);
		notify.setBanned(banned);

		broadcast(null, notify);		
	}

	/**
	 * Envia os dados da conta atualizada conforme a solicitação dos dados vip de uma conta.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param account conta contendo as informações que devem ser enviadas.
	 * @param flag tipo de vip que foi concedido a informações da conta.
	 * @param mapFD código da conexão do servidor de personagem com o servidor de mapa.
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
	 * Envia a um servidor de personagem o resultado da solicitação dos registros de variáveis de uma conta.
	 * Os registros são alocados em uma fila que ordena os elementos através de nós para agilizar.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de personagem.
	 * @param accountID código de identificação da conta do qual as variáveis pertencem.
	 * @param charID código de identificação do personagem que irá receber as variáveis.
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
	 * Repassa todas as informações dos grupos de contas e tipos de acesso VIP a um servidor de personagem.
	 * @param fd conexão do descritor de arquivo do servidor de acesso com o servidor de personagem.
	 * @param groupControl controle de grupos que irá informar os dados dos grupos e acessos VIP.
	 */

	public void sendGroupData(LFileDescriptor fd, GroupControl groupControl)
	{
		SS_GroupData packet = new SS_GroupData();
		packet.setGroups(groupControl.exportGroups());
		packet.setVips(groupControl.exportVips());
		packet.send(fd);
	}
}

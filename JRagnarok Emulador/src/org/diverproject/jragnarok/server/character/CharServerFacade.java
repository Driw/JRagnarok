package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_ACCOUNT_DATA;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_ACCOUNT_INFO;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_ACK_CONNECT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_ALREADY_ONLINE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_AUTH_ACCOUNT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_BAN_NOTIFICATION;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_CHANGE_SEX;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_GLOBAL_REGISTERS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_KEEP_ALIVE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_AH_SYNCRONIZE_IPADDRESS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SS_GROUP_DATA;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ZH_MAP_SERVER_CONNECTION;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ZH_SEND_MAPS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_CHARLIST_REQ;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_CREATE_NEW_CHAR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR2;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR3;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR3_CANCEL;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_DELETE_CHAR3_RESERVED;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_ENTER;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_MAKE_CHAR;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_MAKE_CHAR_NOT_STATS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CH_PING;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logNotice;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.database.MapIndexes;
import org.diverproject.jragnarok.packets.AcknowledgePacket;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.util.lang.HexUtil;
import org.diverproject.util.stream.StreamException;

/**
 * <h1>Servidor de Personagem - Fa�ade</h1>
 *
 * <p>Essa classe � usada para centralizar todos os servi�os e controles do servidor de personagem.
 * Atrav�s dele os servi�os poder�o comunicar-se entre si como tamb�m chamar os controles dispon�veis.
 * Possui m�todos que ir�o realizar a cria��o das inst�ncias e destrui��o das mesmas quando necess�rio.</p>
 *
 * @see ServiceCharClient
 * @see ServiceCharServer
 * @see ServiceCharLogin
 * @see ServiceCharMap
 * @see ServiceCharServerAuth
 * @see AuthMap
 * @see OnlineMap
 * @see CharacterControl
 *
 * @author Andrew
 */

class CharServerFacade
{
	/**
	 * Servi�o para comunica��o inicial com o cliente.
	 */
	private ServiceCharClient clientService;

	/**
	 * Servi�o principal do servidor de personagem.
	 */
	private ServiceCharServer charService;

	/**
	 * Servi�o para comunica��o com o servidor de acesso.
	 */
	private ServiceCharLogin loginService;

	/**
	 * Servi�o para comunica��o com o servidor de mapa.
	 */
	private ServiceCharMap mapService;

	/**
	 * Servi�o para autentica��o dos jogadores no servidor.
	 */
	private ServiceCharServerAuth authService;


	/**
	 * Controle para autentica��o de jogadores online.
	 */
	private AuthMap authMap;

	/**
	 * Controle para dados de personagens online.
	 */
	private OnlineMap onlineMap;

	/**
	 * Controle para gerenciar dados dos personagens.
	 */
	private CharacterControl characterControl;

	/**
	 * Indexa��o dos mapas dispon�veis no servidor.
	 */
	private MapIndexes mapIndexes;

	/**
	 * @return aquisi��o do servi�o para comunica��o inicial com clientes.
	 */

	public ServiceCharClient getCharClient()
	{
		return clientService;
	}

	/**
	 * @return aquisi��o do servi�o principal do servidor de personagem.
	 */

	public ServiceCharServer getCharService()
	{
		return charService;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o com o servidor de acesso.
	 */

	public ServiceCharLogin getLoginService()
	{
		return loginService;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o com o servidor de mapa.
	 */

	public ServiceCharMap getMapService()
	{
		return mapService;
	}

	/**
	 * @return aquisi��o do servi�o para autentica��o dos jogadores no servidor.
	 */

	public ServiceCharServerAuth getAuthService()
	{
		return authService;
	}


	/**
	 * @return aquisi��o do controle para autentica��o dos jogadores online.
	 */

	public AuthMap getAuthMap()
	{
		return authMap;
	}

	/**
	 * @return aquisi��o do controle para dados de personagens online.
	 */

	public OnlineMap getOnlineMap()
	{
		return onlineMap;
	}

	/**
	 * @return aquisi��o do controle dos dados b�sicos dos personagens.
	 */

	public CharacterControl getCharacterControl()
	{
		return characterControl;
	}

	/**
	 * @return aquisi��o da indexa��o dos mapas dispon�veis no servidor.
	 */

	public MapIndexes getMapIndexes()
	{
		return mapIndexes;
	}

	/**
	 * Inicializa as depend�ncias necess�rias para a cria��o do facade no servidor de personagem.
	 * As depend�ncias consistem em classes de controles, servi�os utilizados e cole��es de dados.
	 * @param charServer servidor de personagem do qual est� solicitando a inicializa��o.
	 */

	public void init(CharServer charServer)
	{
		authMap = new AuthMap();
		onlineMap = new OnlineMap(charServer.getMySQL().getConnection());
		characterControl = new CharacterControl(charServer.getMySQL().getConnection());
		mapIndexes = new MapIndexes();

		clientService = new ServiceCharClient(charServer);
		charService = new ServiceCharServer(charServer);
		loginService = new ServiceCharLogin(charServer);
		mapService = new ServiceCharMap(charServer);
		authService = new ServiceCharServerAuth(charServer);

		clientService.init();
		charService.init();
		loginService.init();
		mapService.init();
		authService.init();
	}

	/**
	 * Solicita a destrui��o do facade que ir� chamar o m�todo para destruir suas depend�ncias.
	 * A destrui��o consiste em apenas tornar as depend�ncias inv�lidas e n�o remover as refer�ncias.
	 */

	public void destroy()
	{
		loginService.onlnesSetAllOffline(OnlineCharData.NO_SERVER);

		clientService.destroy();
		charService.destroy();
		loginService.destroy();
		mapService.destroy();
		authService.destroy();

		authMap.clear();
		onlineMap.clear();
	}

	/**
	 * Ap�s a destrui��o das informa��es contidas nas depend�ncias do facade deve remover suas refer�ncias.
	 * Assim ser� poss�vel criar novas depend�ncias caso seja necess�rio sem gasto desnecess�rio de mem�ria.
	 */

	public void destroyed()
	{
		characterControl = null;
		authMap = null;
		onlineMap = null;

		charService = null;
		loginService = null;
		mapService = null;
		clientService = null;
		authService = null;
	}

	/**
	 * Listener utilizado para aplicar a uma conex�o estabelecida no servidor de personagem.
	 * Esse listener pode ser utilizado para remover informa��es quando um jogador ficar offline.
	 * Pode ser ainda ent�o informa��es que devem ser removidas quando o jogador fechar o jogo.
	 */

	public final FileDescriptorListener CLOSE_LISTENER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			CFileDescriptor cfd = (CFileDescriptor) fd;
			CharSessionData sd = cfd.getSessionData();

			if (sd.getID() > 0)
			{
				OnlineCharData online = onlineMap.get(sd.getID());

				if (online == null || online.getCharID() == 0)
					loginService.sendAccountOffline(sd.getID());
			}

			return false;
		}
	};

	/**
	 * Listener usado para receber novas conex�es solicitadas atrav�s do cliente (jogador).
	 * Ir� validar a conex�o e repassar para um m�todo que dever� reconhecer o pacote.
	 */

	public final FileDescriptorListener CLIENT_PARSE = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
				return false;

			CFileDescriptor cfd = (CFileDescriptor) fd;

			// J� foi autenticado, n�o deveria estar aqui
			if (fd.getFlag().is(FileDescriptor.FLAG_EOF) && clientService.parseAlreadyAuth(cfd))
				return true;

			return ackClientPacket(cfd);
		}
	};

	/**
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * Neste caso ir� identificar o comando e que � esperado como fase inicial do cliente.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean ackClientPacket(CFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd, false);

		short command = packetReceivePacketID.getPacketID();

		logDebug("recebendo pacote de cliente (fd: %d, pid: %s).\n", fd.getID(), HexUtil.parseShort(command, 4));

		switch (command)
		{
			case PACKET_CH_ENTER:
				return authService.parse(fd);

			case PACKET_CH_PING:
				charService.keepAlive(fd);
				return true;

			/*
			case PACKET_CH_REQ_IS_VALID_CHARNAME:
			case PACKET_CH_ENTER_CHECKBOT:
			case PACKET_CH_CHECKBOT:
			*/

			case PACKET_ZH_MAP_SERVER_CONNECTION:
				return mapService.parse(fd);
		}

		return ackCharactersPacket(fd, command);
	}

	/**
	 * Procedimento para repassar a conex�o do cliente durante a sele��o de personagens.
	 * O redirecionamento ser� feito de acordo com o tipo de comando recebido por pacote.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param command identifica��o do comando solicitado pelo cliente (jogador).
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean ackCharactersPacket(CFileDescriptor fd, short command)
	{
		switch (command)
		{
			/*
			case PACKET_CH_SELECT_CHAR:
			*/

			case PACKET_CH_MAKE_CHAR:
			case PACKET_CH_CREATE_NEW_CHAR:
			case PACKET_CH_MAKE_CHAR_NOT_STATS:
				charService.makeChar(fd, command);
				return true;

			case PACKET_CH_DELETE_CHAR:
			case PACKET_CH_DELETE_CHAR2:
				charService.deleteCharByEmail(fd, command);
				return true;

			case PACKET_CH_DELETE_CHAR3_RESERVED:
				charService.deleteCharReserved(fd);
				return true;

			case PACKET_CH_DELETE_CHAR3:
				charService.deleteCharByBirthDate(fd);
				return true;

			case PACKET_CH_DELETE_CHAR3_CANCEL:
				charService.deleteCharCancel(fd);
				return true;

			/*
			case PACKET_CH_REQ_CHANGE_CHARACTER_SLOT:
			case PACKET_CH_MAKE_CHAR_NOT_STATS:
			*/

			case PACKET_CH_CHARLIST_REQ:
				return clientService.sendCharsPerPage(fd);
		}

		return ackPincodePacket(fd, command);
	}

	/**
	 * Procedimento para repassar a conex�o do cliente durante a utiliza��o do c�digo PIN.
	 * O redirecionamento ser� feito de acordo com o tipo de comando recebido por pacote.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de personagem.
	 * @param command identifica��o do comando solicitado pelo cliente (jogador).
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean ackPincodePacket(CFileDescriptor fd, short command)
	{
		switch (command)
		{
			/*
			case PACKET_CH_SECOND_PASSWD_ACK:
			case PACKET_CH_MAKE_SECOND_PASSWD:
			case PACKET_CH_EDIT_SECOND_PASSWD:
			case PACKET_CH_AVAILABLE_SECOND_PASSWD:
			*/

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logNotice("fim de conex�o inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	/**
	 * Listener usado para receber pacotes da comunica��o com o servidor de acesso.
	 * Ir� validar a conex�o e repassar para um m�todo que dever� reconhecer o pacote.
	 */

	public final FileDescriptorListener PARSE_LOGIN_SERVER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logWarning("conex�o com o servidor de acesso perdida.\n");
				return false;
			}

			CFileDescriptor cfd = (CFileDescriptor) fd;

			return ackLoginServerPacket(cfd);
		}
	};

	/**
	 * Procedimento para repassar a conex�o do servidor de acesso para os comandos b�sicos.
	 * O redirecionamento ser� feito de acordo com o tipo de comando recebido por pacote.
	 * A identifica��o do comando � feito neste m�todo e pode repassar a outras fun��es.
	 * @param fd conex�o do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean ackLoginServerPacket(CFileDescriptor fd)
	{
		AcknowledgePacket ack = new AcknowledgePacket();
		ack.receive(fd, false);

		short command = ack.getPacketID();

		switch (command)
		{
			case PACKET_AH_ACK_CONNECT:
				return loginService.parseLoginResult(fd);

			case PACKET_AH_KEEP_ALIVE:
				loginService.keepAlive(fd);
				return true;

			case PACKET_AH_GLOBAL_REGISTERS:
				loginService.reqGlobalAccountReg(fd);
				return true;

			case PACKET_AH_ALREADY_ONLINE:
				loginService.alreadyOnline(fd);
				return true;

			case PACKET_AH_SYNCRONIZE_IPADDRESS:
				try {
					fd.getPacketBuilder().newOutputPacket("SYNCRONIZE_IPADDRESS").skipe(4);
					return true;
				} catch (StreamException e) {
					return false;
				}

			case PACKET_SS_GROUP_DATA:
				loginService.parseGroupData(fd);
				return true;
		}

		return ackAccountPacket(fd, command);
	}

	/**
	 * Procedimento para repassar a conex�o do servidor de acesso para gerenciar contas.
	 * O redirecionamento ser� feito de acordo com o tipo de comando recebido por pacote.
	 * @param fd conex�o do descritor de arquivo do servidor de acesso com o servidor.
	 * @param command identifica��o do comando solicitado pelo servidor de acesso.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean ackAccountPacket(CFileDescriptor fd, short command)
	{
		switch (command)
		{
			case PACKET_AH_CHANGE_SEX:
				return loginService.reqChangeSex(fd);

			case PACKET_AH_BAN_NOTIFICATION:
				loginService.banNofitication(fd);
				return true;

			case PACKET_AH_AUTH_ACCOUNT:
				return loginService.parseAuthAccount(fd);

			case PACKET_AH_ACCOUNT_DATA:
				return loginService.parseAccountData(fd);

			case PACKET_AH_ACCOUNT_INFO:
				loginService.parseAccountInfo(fd);
				return true;

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logNotice("fim de conex�o inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	/**
	 * Listener usado para receber pacotes da comunica��o com o servidor de mapa.
	 * Ir� validar a conex�o e repassar para um m�todo que dever� reconhecer o pacote.
	 */

	public final FileDescriptorListener PARSE_MAP_SERVER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logWarning("conex�o com o servidor de mapa perdida.\n");
				return false;
			}

			CFileDescriptor cfd = (CFileDescriptor) fd;

			return ackMapServerPacket(cfd);
		}
	};

	/**
	 * Procedimento para repassar a conex�o do servidor de mapa para os comandos b�sicos.
	 * O redirecionamento ser� feito de acordo com o tipo de comando recebido por pacote.
	 * A identifica��o do comando � feito neste m�todo e pode repassar a outras fun��es.
	 * @param fd conex�o do descritor de arquivo do servidor de mapa com o servidor.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean ackMapServerPacket(CFileDescriptor fd)
	{
		AcknowledgePacket ack = new AcknowledgePacket();
		ack.receive(fd, false);

		short command = ack.getPacketID();

		switch (command)
		{
			case PACKET_ZH_SEND_MAPS:
				mapService.receiveMapIndexes(fd);
				return true;

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logNotice("fim de conex�o inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}
}

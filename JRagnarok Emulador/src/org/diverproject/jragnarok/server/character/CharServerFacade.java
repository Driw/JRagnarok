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
import org.diverproject.jragnarok.packets.AcknowledgePacket;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.util.lang.HexUtil;
import org.diverproject.util.stream.StreamException;

public class CharServerFacade
{
	/**
	 * Serviço para comunicação inicial com o cliente.
	 */
	private ServiceCharClient clientService;

	/**
	 * Serviço principal do servidor de personagem.
	 */
	private ServiceCharServer charService;

	/**
	 * Serviço para comunicação com o servidor de acesso.
	 */
	private ServiceCharLogin loginService;

	/**
	 * Serviço para comunicação com o servidor de mapa.
	 */
	private ServiceCharMap mapService;

	/**
	 * Serviço para autenticação dos jogadores no servidor.
	 */
	private ServiceCharServerAuth authService;


	/**
	 * Controle para autenticação de jogadores online.
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
	 * @return aquisição do serviço para comunicação inicial com clientes.
	 */

	public ServiceCharClient getCharClient()
	{
		return clientService;
	}

	/**
	 * @return aquisição do serviço principal do servidor de personagem.
	 */

	public ServiceCharServer getCharService()
	{
		return charService;
	}

	/**
	 * @return aquisição do serviço para comunicação com o servidor de acesso.
	 */

	public ServiceCharLogin getLoginService()
	{
		return loginService;
	}

	/**
	 * @return aquisição do serviço para comunicação com o servidor de mapa.
	 */

	public ServiceCharMap getMapService()
	{
		return mapService;
	}

	/**
	 * @return aquisição do serviço para autenticação dos jogadores no servidor.
	 */

	public ServiceCharServerAuth getAuthService()
	{
		return authService;
	}


	/**
	 * @return aquisição do controle para autenticação dos jogadores online.
	 */

	public AuthMap getAuthMap()
	{
		return authMap;
	}

	/**
	 * @return aquisição do controle para dados de personagens online.
	 */

	public OnlineMap getOnlineMap()
	{
		return onlineMap;
	}

	/**
	 * @return aquisição do controle dos dados básicos dos personagens.
	 */

	public CharacterControl getCharacterControl()
	{
		return characterControl;
	}

	public void init(CharServer charServer)
	{
		authMap = new AuthMap();
		onlineMap = new OnlineMap(charServer.getMySQL().getConnection());
		characterControl = new CharacterControl(charServer.getMySQL().getConnection());

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

	public void destroy()
	{
		clientService.destroy();
		charService.destroy();
		loginService.destroy();
		mapService.destroy();
		authService.destroy();

		authMap.clear();
		onlineMap.clear();
	}

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

	public final FileDescriptorListener CLOSE_LISTENER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			CFileDescriptor cfd = (CFileDescriptor) fd;
			CharSessionData sd = cfd.getSessionData();

			if (sd.getID() > 0)
			{
				/*
				 * TODO Confirmar se ao entrar no servidor de mapa a conexão será fechada também

				OnlineCharData online = onlineMap.get(sd.getID());

				if (online != null)
					onlineMap.remove(online);
				 */

				loginService.setAccountOffline(sd.getID());
			}

			return false;
		}
	};

	public final FileDescriptorListener CLIENT_PARSE = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			logDebug("recebendo pacote de cliente (fd: %d).\n", fd.getID());

			CFileDescriptor cfd = (CFileDescriptor) fd;

			if (!fd.isConnected())
				return false;

			// Já foi autenticado, não deveria estar aqui
			if (fd.getFlag().is(FileDescriptor.FLAG_EOF) && clientService.parseAlreadyAuth(cfd))
				return true;

			return ackClientPacket(cfd);
		}
	};

	private boolean ackClientPacket(CFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd, false);

		short command = packetReceivePacketID.getPacketID();

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
			case PACKET_CH_PARSE_MAP_LOGIN:
			*/
		}

		return ackCharactersPacket(fd, command);
	}

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
				logNotice("fim de conexão inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	public final FileDescriptorListener PARSE_LOGIN_SERVER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logWarning("conexão com o servidor de acesso perdida.\n");
				return false;
			}

			CFileDescriptor cfd = (CFileDescriptor) fd;

			return ackLoginServerPacket(cfd);
		}
	};

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
				logNotice("fim de conexão inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}
}

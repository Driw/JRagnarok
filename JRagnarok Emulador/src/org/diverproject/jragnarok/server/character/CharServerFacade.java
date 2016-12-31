package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_ALREADY_ONLINE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_CHAR_SERVER_SELECTED;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_BAN_NOTIFICATION;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_CHANGE_SEX;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_CHARLIST;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_REQ_KEEP_ALIVE;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_ACCOUNT_DATA;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_ACCOUNT_INFO;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_AUTH_ACCOUNT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_CHAR_SERVER_CONNECT;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_RES_GLOBAL_REGISTERS;
import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_SYNCRONIZE_IPADDRESS;
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

	public void create(CharServer charServer)
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

	public final FileDescriptorListener CLIENT_PARSE = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			logDebug("recebendo pacote de cliente (fd: %d).\n", fd.getID());

			CFileDescriptor cfd = (CFileDescriptor) fd;

			if (!fd.isConnected())
				return false;

			// J� foi autenticado, n�o deveria estar aqui
			if (fd.getFlag().is(FileDescriptor.FLAG_EOF) && clientService.parseAlreadyAuth(cfd))
				return true;

			return ackClientPacket(cfd);
		}
	};

	public boolean ackClientPacket(CFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd, false);

		short command = packetReceivePacketID.getPacketID();

		switch (command)
		{
			case PACKET_CHAR_SERVER_SELECTED:
				return authService.parse(fd);

			case PACKET_REQ_CHARLIST:
				return clientService.sendCharsPerPage(fd);

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logNotice("fim de conex�o inesperado (pacote: 0x%s, ip: %s)\n", packet, address);
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
				logWarning("conex�o com o servidor de acesso perdida.\n");
				return false;
			}

			CFileDescriptor cfd = (CFileDescriptor) fd;
			loginService.parsePing(cfd);

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
			case PACKET_REQ_KEEP_ALIVE:
				return loginService.keepAlive(fd);

			case PACKET_RES_GLOBAL_REGISTERS:
				loginService.reqGlobalAccountReg(fd);
				return true;

			case PACKET_ALREADY_ONLINE:
				loginService.alreadyOnline(fd);
				return true;

			case PACKET_SYNCRONIZE_IPADDRESS:
				try {
					fd.getPacketBuilder().newOutputPacket("SYNCRONIZE_IPADDRESS").skipe(4);
					return true;
				} catch (StreamException e) {
					return false;
				}
		}

		return ackPlayerRequestPacket(fd, command);
	}

	private boolean ackPlayerRequestPacket(CFileDescriptor fd, short command)
	{
		switch (command)
		{
			case PACKET_REQ_CHANGE_SEX:
				return loginService.reqChangeSex(fd);

			case PACKET_REQ_BAN_NOTIFICATION:
				loginService.banNofitication(fd);
				return true;
		}

		return ackPlayerResultPacket(fd, command);
	}

	private boolean ackPlayerResultPacket(CFileDescriptor fd, short command)
	{
		switch (command)
		{
			case PACKET_RES_CHAR_SERVER_CONNECT:
				return loginService.parseLoginResult(fd);

			case PACKET_RES_AUTH_ACCOUNT:
				return loginService.parseAuthAccount(fd);

			case PACKET_RES_ACCOUNT_DATA:
				return loginService.parseAccountData(fd);

			case PACKET_RES_ACCOUNT_INFO:
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
}

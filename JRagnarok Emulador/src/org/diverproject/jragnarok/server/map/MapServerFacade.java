package org.diverproject.jragnarok.server.map;

import static org.diverproject.jragnarok.packets.RagnarokPacket.PACKET_HZ_RESULT_MAP_CONNECTION;
import static org.diverproject.log.LogSystem.logDebug;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnaork.database.MapIndexes;
import org.diverproject.jragnarok.packets.AcknowledgePacket;
import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.FileDescriptorListener;
import org.diverproject.util.lang.HexUtil;

public class MapServerFacade
{
	/**
	 * Servi�o para gerenciamento do servidor de mapa.
	 */
	private ServiceMapServer serviceMapServer;

	/**
	 * Servi�o para comunica��o com o servidor de personagem.
	 */
	private ServiceMapChar serviceMapChar;

	/**
	 * Indexa��o de todos os mapas do jogo.
	 */
	private MapIndexes mapIndexes;

	/**
	 * Mapeamento das autentica��es no servidor de mapas.
	 */
	private AuthMap authMap;

	/**
	 * @return aquisi��o do servi�o para gerenciamento do servidor de mapa.
	 */

	public ServiceMapServer getServiceMapServer()
	{
		return serviceMapServer;
	}

	/**
	 * @return aquisi��o do servi�o para comunica��o com o servidor de personagem.
	 */

	public ServiceMapChar getServiceMapChar()
	{
		return serviceMapChar;
	}

	/**
	 * @return aquisi��o da indexa��o de todos os maps do jogo.
	 */

	public MapIndexes getMapIndexes()
	{
		return mapIndexes;
	}

	/**
	 * @return aquisi��o do mapeamento das autentica��es no servidor de mapas.
	 */

	public AuthMap getAuthMap()
	{
		return authMap;
	}

	/**
	 * Cria todas as inst�ncias dos servi�os e controles para um servidor de mapa.
	 * @param mapServer refer�ncia do servidor de mapa que ir� us�-los.
	 */

	public void init(MapServer mapServer)
	{
		serviceMapServer = new ServiceMapServer(mapServer);
		serviceMapChar = new ServiceMapChar(mapServer);

		mapIndexes = new MapIndexes();
		authMap = new AuthMap();

		serviceMapServer.init();
		serviceMapChar.init();
	}

	/**
	 * Procedimento de preparo para a destrui��o deste fa�ade utilizado pelo servidor de mapa passado.
	 * Deve fechar todas as conex�es com os servidores de personagem que estiverem estabelecidas.
	 * Tamb�m deve destruir todos os servi�os e controles que est�o aqui instanciados.
	 * @param mapServer servidor de mapa que est� chamando essa opera��o do fa�ade.
	 */

	public void destroy(MapServer mapServer)
	{
		serviceMapServer.destroy();

		mapIndexes.clear();
	}

	/**
	 * Destr�i todos os servi�os e controles removendo a refer�ncia de seus objetos.
	 */

	public void destroyed()
	{
		serviceMapServer = null;

		mapIndexes = null;
	}

	public final FileDescriptorListener CLOSE_LISTENER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			// TODO Auto-generated method stub
			return false;
		}
	};

	/**
	 * Listener usado para receber novas conex�es solicitadas atrav�s do cliente (jogador).
	 * Ir� validar a conex�o e repassar para um m�todo que dever� reconhecer o pacote.
	 */

	public final FileDescriptorListener PARSE_CLIENT = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
				return false;

			MFileDescriptor mfd = (MFileDescriptor) fd;

			// J� conectou, verificar se est� banido
			if (mfd.getID() == 0)
				;// TODO

			return ackClientPacket(mfd);
		}
	};

	/**
	 * Procedimento para repassar a conex�o do servidor de personagem aos comandos b�sicos.
	 * O redirecionamento ser� feito de acordo com o tipo de comando recebido por pacote.
	 * A identifica��o do comando � feito neste m�todo e pode repassar a outras fun��es.
	 * @param fd conex�o do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	public boolean ackClientPacket(MFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd);

		short command = packetReceivePacketID.getPacketID();

		logDebug("recebendo pacote de um cliente (fd: %d, pid: %s).\n", fd.getID(), HexUtil.parseShort(command, 4));

		switch (command)
		{
			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logWarning("pacote desconhecido recebido (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}

	/**
	 * Listener usado para receber pacotes da comunica��o com o servidor de personagem.
	 * Ir� validar a conex�o e repassar para um m�todo que dever� reconhecer o pacote.
	 */

	public final FileDescriptorListener PARSE_CHAR_SERVER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logWarning("conex�o com o servidor de personagem perdida.\n");
				return false;
			}

			MFileDescriptor mfd = (MFileDescriptor) fd;

			return ackCharServerPacket(mfd);
		}
	};

	/**
	 * Procedimento para repassar a conex�o do servidor de personagem aos comandos b�sicos.
	 * O redirecionamento ser� feito de acordo com o tipo de comando recebido por pacote.
	 * A identifica��o do comando � feito neste m�todo e pode repassar a outras fun��es.
	 * @param fd conex�o do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se o pacote recebido for de um tipo v�lido para an�lise.
	 */

	private boolean ackCharServerPacket(MFileDescriptor fd)
	{
		AcknowledgePacket packetReceivePacketID = new AcknowledgePacket();
		packetReceivePacketID.receive(fd, false);

		short command = packetReceivePacketID.getPacketID();

		logDebug("recebendo pacote do servidor de personagem (fd: %d, pid: %s).\n", fd.getID(), HexUtil.parseShort(command, 4));

		switch (command)
		{
			case PACKET_HZ_RESULT_MAP_CONNECTION:
				return serviceMapChar.parseResultConnection(fd);

			default:
				String packet = HexUtil.parseInt(command, 4);
				String address = fd.getAddressString();
				logWarning("pacote desconhecido recebido (pacote: 0x%s, ip: %s)\n", packet, address);
				fd.close();
				return false;
		}
	}
}

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
	 * Serviço para gerenciamento do servidor de mapa.
	 */
	private ServiceMapServer serviceMapServer;

	/**
	 * Serviço para comunicação com o servidor de personagem.
	 */
	private ServiceMapChar serviceMapChar;

	/**
	 * Indexação de todos os mapas do jogo.
	 */
	private MapIndexes mapIndexes;

	/**
	 * Mapeamento das autenticações no servidor de mapas.
	 */
	private AuthMap authMap;

	/**
	 * @return aquisição do serviço para gerenciamento do servidor de mapa.
	 */

	public ServiceMapServer getServiceMapServer()
	{
		return serviceMapServer;
	}

	/**
	 * @return aquisição do serviço para comunicação com o servidor de personagem.
	 */

	public ServiceMapChar getServiceMapChar()
	{
		return serviceMapChar;
	}

	/**
	 * @return aquisição da indexação de todos os maps do jogo.
	 */

	public MapIndexes getMapIndexes()
	{
		return mapIndexes;
	}

	/**
	 * @return aquisição do mapeamento das autenticações no servidor de mapas.
	 */

	public AuthMap getAuthMap()
	{
		return authMap;
	}

	/**
	 * Cria todas as instâncias dos serviços e controles para um servidor de mapa.
	 * @param mapServer referência do servidor de mapa que irá usá-los.
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
	 * Procedimento de preparo para a destruição deste façade utilizado pelo servidor de mapa passado.
	 * Deve fechar todas as conexões com os servidores de personagem que estiverem estabelecidas.
	 * Também deve destruir todos os serviços e controles que estão aqui instanciados.
	 * @param mapServer servidor de mapa que está chamando essa operação do façade.
	 */

	public void destroy(MapServer mapServer)
	{
		serviceMapServer.destroy();

		mapIndexes.clear();
	}

	/**
	 * Destrói todos os serviços e controles removendo a referência de seus objetos.
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
	 * Listener usado para receber novas conexões solicitadas através do cliente (jogador).
	 * Irá validar a conexão e repassar para um método que deverá reconhecer o pacote.
	 */

	public final FileDescriptorListener PARSE_CLIENT = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
				return false;

			MFileDescriptor mfd = (MFileDescriptor) fd;

			// Já conectou, verificar se está banido
			if (mfd.getID() == 0)
				;// TODO

			return ackClientPacket(mfd);
		}
	};

	/**
	 * Procedimento para repassar a conexão do servidor de personagem aos comandos básicos.
	 * O redirecionamento será feito de acordo com o tipo de comando recebido por pacote.
	 * A identificação do comando é feito neste método e pode repassar a outras funções.
	 * @param fd conexão do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se o pacote recebido for de um tipo válido para análise.
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
	 * Listener usado para receber pacotes da comunicação com o servidor de personagem.
	 * Irá validar a conexão e repassar para um método que deverá reconhecer o pacote.
	 */

	public final FileDescriptorListener PARSE_CHAR_SERVER = new FileDescriptorListener()
	{
		@Override
		public boolean onCall(FileDescriptor fd) throws RagnarokException
		{
			if (!fd.isConnected())
			{
				logWarning("conexão com o servidor de personagem perdida.\n");
				return false;
			}

			MFileDescriptor mfd = (MFileDescriptor) fd;

			return ackCharServerPacket(mfd);
		}
	};

	/**
	 * Procedimento para repassar a conexão do servidor de personagem aos comandos básicos.
	 * O redirecionamento será feito de acordo com o tipo de comando recebido por pacote.
	 * A identificação do comando é feito neste método e pode repassar a outras funções.
	 * @param fd conexão do descritor de arquivo do servidor de acesso com o servidor.
	 * @return true se o pacote recebido for de um tipo válido para análise.
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

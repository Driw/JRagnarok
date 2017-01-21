package org.diverproject.jragnarok.server.map;

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
	 * Serviço de gerenciamento do servidor de mapa.
	 */
	private ServiceMapServer serviceMapServer;

	/**
	 * Indexação de todos os mapas do jogo.
	 */
	private MapIndexes mapIndexes;

	/**
	 * @return aquisição do serviço para gerenciamento do servidor de mapa.
	 */

	public ServiceMapServer getServiceMapServer()
	{
		return serviceMapServer;
	}

	/**
	 * @return aquisição da indexação de todos os maps do jogo.
	 */

	public MapIndexes getMapIndexes()
	{
		return mapIndexes;
	}

	/**
	 * Cria todas as instâncias dos serviços e controles para um servidor de mapa.
	 * @param mapServer referência do servidor de mapa que irá usá-los.
	 */

	public void init(MapServer mapServer)
	{
		serviceMapServer = new ServiceMapServer(mapServer);

		mapIndexes = new MapIndexes();

		serviceMapServer.init();
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
	 * Listener usado para receber novas conexões solicitadas com o servidor de mapa.
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
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despachá-lo.
	 * Neste caso irá identificar o comando e que é esperado como fase inicial do cliente.
	 * @param fd conexão do descritor de arquivo do cliente com o servidor de mapa.
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
}

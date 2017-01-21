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
	 * Servi�o de gerenciamento do servidor de mapa.
	 */
	private ServiceMapServer serviceMapServer;

	/**
	 * Indexa��o de todos os mapas do jogo.
	 */
	private MapIndexes mapIndexes;

	/**
	 * @return aquisi��o do servi�o para gerenciamento do servidor de mapa.
	 */

	public ServiceMapServer getServiceMapServer()
	{
		return serviceMapServer;
	}

	/**
	 * @return aquisi��o da indexa��o de todos os maps do jogo.
	 */

	public MapIndexes getMapIndexes()
	{
		return mapIndexes;
	}

	/**
	 * Cria todas as inst�ncias dos servi�os e controles para um servidor de mapa.
	 * @param mapServer refer�ncia do servidor de mapa que ir� us�-los.
	 */

	public void init(MapServer mapServer)
	{
		serviceMapServer = new ServiceMapServer(mapServer);

		mapIndexes = new MapIndexes();

		serviceMapServer.init();
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
	 * Listener usado para receber novas conex�es solicitadas com o servidor de mapa.
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
	 * Procedimento chamado para identificar o tipo de pacote que encontrado e despach�-lo.
	 * Neste caso ir� identificar o comando e que � esperado como fase inicial do cliente.
	 * @param fd conex�o do descritor de arquivo do cliente com o servidor de mapa.
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
}

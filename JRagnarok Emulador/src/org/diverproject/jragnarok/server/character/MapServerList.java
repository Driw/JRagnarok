package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_SERVERS;
import static org.diverproject.util.Util.indexOn;

import java.util.Iterator;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.LoopList;

/**
 * <h1>Lista dos Servidores de Mapa</h1>
 *
 * <p>Coleção para armazenar informações dos servidores de mapa conectados ao de personagem.
 * Os servidores de mapa já autorizados pelo servidor de personagem são listados aqui.
 * A classe permite adicionar e remover os servidores como clientes e iterá-los.</p>
 *
 * @see List
 * @see LoopList
 * @see Iterable
 * @see ClientMapServer
 *
 * @author Andrew Mello
 *
 */

public class MapServerList implements Iterable<ClientMapServer>
{
	/**
	 * Lista que irá conter os servidores de mapa como clientes.
	 */
	private List<ClientMapServer> servers;

	/**
	 * Cria uma nova coleção para armazenar os servidores de mapa conectados.
	 * Inicializa a lista através da estrutura de loop, evitando espaço em branco.
	 */

	public MapServerList()
	{
		servers = new LoopList<>(MAX_SERVERS);
	}

	/**
	 * Adiciona um novo cliente de um servidor de mapa como acessada.
	 * @param server referência do cliente que representa o servidor.
	 * @return true se adicionado ou false caso contrário.
	 */

	public boolean add(ClientMapServer server)
	{
		if (!servers.contains(server))
			if (servers.add(server))
			{
				server.setID(indexOn(servers, server));
				return true;
			}

		return false;
	}

	/**
	 * Remove um cliente existente de um servidor de mapa já acessado.
	 * @param server referência do cliente que representa o servidor.
	 */

	public void remove(ClientMapServer server)
	{
		servers.remove(server);
	}

	/**
	 * Obtém um determinado servidor da lista de servidores acessados.
	 * @param id código de identificação do servidor desejado.
	 * @return objeto contendo os dados do servidor ou null se id inválido.
	 */

	public ClientMapServer get(int id)
	{
		return servers.get(id);
	}

	/**
	 * Obtém um determinado servidor da lista de servidores acessados.
	 * @param fd sessão da conexão do cliente esperado como servidor de mapa.
	 * @return objeto contendo os dados do servidor ou null se a sessão for inválida.
	 */

	public ClientMapServer get(FileDescriptor fd)
	{
		for (ClientMapServer server : servers)
			if (server.getFileDescriptor().equals(fd))
				return server;

		return null;
	}

	/**
	 * O tamanho dessa coleção representa a quantidade de clientes de
	 * servidores de mapa que foram listados como acessados no mesmo.
	 * @return aquisição da quantidade de clientes armazenados.
	 */

	public int size()
	{
		return servers.size();
	}

	/**
	 * Verifica se a lista para servidores de mapa está cheia ou não.
	 * @return trure se estiver cheia ou false caso contrário.
	 */

	public boolean isFull()
	{
		return servers.size() == servers.length();
	}

	/**
	 * Remove todos os servidores de mapa listados.
	 */

	public void clear()
	{
		servers.clear();
	}

	@Override
	public Iterator<ClientMapServer> iterator()
	{
		synchronized (servers)
		{
			return servers.iterator();
		}
	}
}

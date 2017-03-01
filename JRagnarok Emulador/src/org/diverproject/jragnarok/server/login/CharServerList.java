package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnaork.JRagnarokConstants.MAX_SERVERS;

import java.util.Iterator;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.LoopList;

/**
 * <h1>Servidor de Personagens em Acesso</h1>
 *
 * <p>Coleção para armazenar informações dos servidores de personagens conectados.
 * Os servidores de personagens que já acessaram o servidor de acesso são listados aqui.
 * A classe permite adicionar e remover os servidores como clientes e iterá-los.</p>
 *
 * @see List
 * @see LoopList
 * @see Iterable
 * @see ClientCharServer
 *
 * @author Andrew Mello
 *
 */

public class CharServerList implements Iterable<ClientCharServer>
{
	/**
	 * Lista que irá conter os servidores de personagens como clientes.
	 */
	private List<ClientCharServer> servers;

	/**
	 * Cria uma nova coleção para armazenar os servidores de personagens conectados.
	 * Inicializa a lista através da estrutura de loop, evitando espaço em branco.
	 */

	public CharServerList()
	{
		servers = new LoopList<>(MAX_SERVERS);
	}

	/**
	 * Adiciona um novo cliente de um servidor de personagens como acessada.
	 * @param server referência do cliente que representa o servidor.
	 */

	public void add(ClientCharServer server)
	{
		if (!servers.contains(server))
			servers.add(server);

		for (int i = 0; i < servers.size(); i++)
			if (server.equals(servers.get(i)))
			{
				server.setID(i);
				return;
			}
	}

	/**
	 * Remove um cliente existente de um servidor de personagens já acessado.
	 * @param server referência do cliente que representa o servidor.
	 */

	public void remove(ClientCharServer server)
	{
		servers.remove(server);
	}

	/**
	 * Obtém um determinado servidor da lista de servidores acessados.
	 * @param id código de identificação do servidor desejado.
	 * @return objeto contendo os dados do servidor ou null se id inválido.
	 */

	public ClientCharServer get(int id)
	{
		return servers.get(id);
	}

	/**
	 * Obtém um determinado servidor da lista de servidores acessados.
	 * @param fd sessão da conexão do cliente esperado como servidor de personagem.
	 * @return objeto contendo os dados do servidor ou null se a sessão for inválida.
	 */

	public ClientCharServer get(FileDescriptor fd)
	{
		for (ClientCharServer server : servers)
			if (server.getFileDecriptor().equals(fd))
				return server;

		return null;
	}

	/**
	 * O tamanho dessa coleção representa a quantidade de clientes de
	 * servidores de personagens que foram listados como acessados no mesmo.
	 * @return aquisição da quantidade de clientes armazenados.
	 */

	public int size()
	{
		return servers.size();
	}

	/**
	 * Remove todos os servidores de personagens listados.
	 */

	public void clear()
	{
		servers.clear();
	}

	@Override
	public Iterator<ClientCharServer> iterator()
	{
		synchronized (servers)
		{
			return servers.iterator();
		}
	}
}

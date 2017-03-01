package org.diverproject.jragnarok.server.login;

import static org.diverproject.jragnaork.JRagnarokConstants.MAX_SERVERS;

import java.util.Iterator;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.collection.List;
import org.diverproject.util.collection.abstraction.LoopList;

/**
 * <h1>Servidor de Personagens em Acesso</h1>
 *
 * <p>Cole��o para armazenar informa��es dos servidores de personagens conectados.
 * Os servidores de personagens que j� acessaram o servidor de acesso s�o listados aqui.
 * A classe permite adicionar e remover os servidores como clientes e iter�-los.</p>
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
	 * Lista que ir� conter os servidores de personagens como clientes.
	 */
	private List<ClientCharServer> servers;

	/**
	 * Cria uma nova cole��o para armazenar os servidores de personagens conectados.
	 * Inicializa a lista atrav�s da estrutura de loop, evitando espa�o em branco.
	 */

	public CharServerList()
	{
		servers = new LoopList<>(MAX_SERVERS);
	}

	/**
	 * Adiciona um novo cliente de um servidor de personagens como acessada.
	 * @param server refer�ncia do cliente que representa o servidor.
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
	 * Remove um cliente existente de um servidor de personagens j� acessado.
	 * @param server refer�ncia do cliente que representa o servidor.
	 */

	public void remove(ClientCharServer server)
	{
		servers.remove(server);
	}

	/**
	 * Obt�m um determinado servidor da lista de servidores acessados.
	 * @param id c�digo de identifica��o do servidor desejado.
	 * @return objeto contendo os dados do servidor ou null se id inv�lido.
	 */

	public ClientCharServer get(int id)
	{
		return servers.get(id);
	}

	/**
	 * Obt�m um determinado servidor da lista de servidores acessados.
	 * @param fd sess�o da conex�o do cliente esperado como servidor de personagem.
	 * @return objeto contendo os dados do servidor ou null se a sess�o for inv�lida.
	 */

	public ClientCharServer get(FileDescriptor fd)
	{
		for (ClientCharServer server : servers)
			if (server.getFileDecriptor().equals(fd))
				return server;

		return null;
	}

	/**
	 * O tamanho dessa cole��o representa a quantidade de clientes de
	 * servidores de personagens que foram listados como acessados no mesmo.
	 * @return aquisi��o da quantidade de clientes armazenados.
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

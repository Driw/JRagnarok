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
 * <p>Cole��o para armazenar informa��es dos servidores de mapa conectados ao de personagem.
 * Os servidores de mapa j� autorizados pelo servidor de personagem s�o listados aqui.
 * A classe permite adicionar e remover os servidores como clientes e iter�-los.</p>
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
	 * Lista que ir� conter os servidores de mapa como clientes.
	 */
	private List<ClientMapServer> servers;

	/**
	 * Cria uma nova cole��o para armazenar os servidores de mapa conectados.
	 * Inicializa a lista atrav�s da estrutura de loop, evitando espa�o em branco.
	 */

	public MapServerList()
	{
		servers = new LoopList<>(MAX_SERVERS);
	}

	/**
	 * Adiciona um novo cliente de um servidor de mapa como acessada.
	 * @param server refer�ncia do cliente que representa o servidor.
	 * @return true se adicionado ou false caso contr�rio.
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
	 * Remove um cliente existente de um servidor de mapa j� acessado.
	 * @param server refer�ncia do cliente que representa o servidor.
	 */

	public void remove(ClientMapServer server)
	{
		servers.remove(server);
	}

	/**
	 * Obt�m um determinado servidor da lista de servidores acessados.
	 * @param id c�digo de identifica��o do servidor desejado.
	 * @return objeto contendo os dados do servidor ou null se id inv�lido.
	 */

	public ClientMapServer get(int id)
	{
		return servers.get(id);
	}

	/**
	 * Obt�m um determinado servidor da lista de servidores acessados.
	 * @param fd sess�o da conex�o do cliente esperado como servidor de mapa.
	 * @return objeto contendo os dados do servidor ou null se a sess�o for inv�lida.
	 */

	public ClientMapServer get(FileDescriptor fd)
	{
		for (ClientMapServer server : servers)
			if (server.getFileDescriptor().equals(fd))
				return server;

		return null;
	}

	/**
	 * O tamanho dessa cole��o representa a quantidade de clientes de
	 * servidores de mapa que foram listados como acessados no mesmo.
	 * @return aquisi��o da quantidade de clientes armazenados.
	 */

	public int size()
	{
		return servers.size();
	}

	/**
	 * Verifica se a lista para servidores de mapa est� cheia ou n�o.
	 * @return trure se estiver cheia ou false caso contr�rio.
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

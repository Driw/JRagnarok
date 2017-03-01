package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnaork.JRagnarokConstants.MAX_MAP_PER_SERVER;
import static org.diverproject.util.Util.size;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Servidor de Mapa como Cliente</h1>
 *
 * <p>Objeto que contém informações básicas para conexão do servidor de acesso com um servidor de mapa.
 * As informações deste consistem em: conexão socket (descritor de arquivo) com o servidor de mapa,
 * endereço de IP, porta para conexão, quantidade de personagens online, lista de mapas no servidor.</p>
 *
 * @see CFileDescriptor
 * @see InternetProtocol
 *
 * @author Andrew
 */

public class ClientMapServer
{
	/**
	 * Identificação do servidor de mapa no servidor de personagem.
	 */
	private int id;

	/**
	 * Arquivo descritor de conexão com o servidor de mapa.
	 */
	private CFileDescriptor fd;

	/**
	 * Endereço de IP da conexão estabelecida.
	 */
	private InternetProtocol ip;

	/**
	 * Porta para conexão com o servidor de mapa.
	 */
	private short port;

	/**
	 * Quantidade de personagens online no servidor.
	 */
	private short users;

	/**
	 * Vetor com o índice de todos os mapas disponíveis no servidor.
	 */
	private Short maps[];

	/**
	 * Cria uma nova instância de um cliente para representação do servidor de mapa no servidor de personagem.
	 * Será necessário passar um arquivo descritor utilizado para realizar a conexão entre os servidores.
	 * @param fileDecriptor referência da conexão do servidor de personagem com o servidor de mapa.
	 */

	public ClientMapServer(CFileDescriptor fileDecriptor)
	{
		this.id = -1;
		this.fd = fileDecriptor;
		this.maps = new Short[MAX_MAP_PER_SERVER];
	}

	/**
	 * @return aquisição da identificação do servidor de mapa no servidor de personagem.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id identificação do servidor de mapa no servidor de personagem.
	 */

	public void setID(int id)
	{
		if (this.id == -1)
			this.id = id;
	}

	/**
	 * @return aquisição da conexão do servidor de personagem com o servidor de mapa.
	 */

	public CFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	/**
	 * @return aquisição do endereço de IP do servidor de mapa.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * @param serverIP endereço de IP do servidor de mapa.
	 */

	public void setIP(InternetProtocol serverIP)
	{
		this.ip = serverIP;
	}

	/**
	 * @return aquisição do número da porta para conexão com o servidor de mapa.
	 */

	public short getPort()
	{
		return port;
	}

	/**
	 * @param port número da porta para conexão com o servidor de mapa.
	 */

	public void setPort(short port)
	{
		this.port = port;
	}

	/**
	 * @return aquisição do número de personagens online no servidor de mapa.
	 */

	public short getUsers()
	{
		return users;
	}

	/**
	 * @param users número de personagens online no servidor de mapa.
	 */

	public void setUsers(short users)
	{
		this.users = users;
	}

	/**
	 * @return aquisição do vetor contendo os mapas disponíveis no servidor de mapas.
	 */

	public Short[] getMaps()
	{
		return maps;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("fd", fd.getID());
		description.append("ip", ip);
		description.append("port", port);
		description.append("online", users);
		description.append("maps", size((Object[]) maps));

		return description.toString();
	}
}

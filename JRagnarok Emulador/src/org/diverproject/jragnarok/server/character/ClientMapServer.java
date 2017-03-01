package org.diverproject.jragnarok.server.character;

import static org.diverproject.jragnaork.JRagnarokConstants.MAX_MAP_PER_SERVER;
import static org.diverproject.util.Util.size;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Servidor de Mapa como Cliente</h1>
 *
 * <p>Objeto que cont�m informa��es b�sicas para conex�o do servidor de acesso com um servidor de mapa.
 * As informa��es deste consistem em: conex�o socket (descritor de arquivo) com o servidor de mapa,
 * endere�o de IP, porta para conex�o, quantidade de personagens online, lista de mapas no servidor.</p>
 *
 * @see CFileDescriptor
 * @see InternetProtocol
 *
 * @author Andrew
 */

public class ClientMapServer
{
	/**
	 * Identifica��o do servidor de mapa no servidor de personagem.
	 */
	private int id;

	/**
	 * Arquivo descritor de conex�o com o servidor de mapa.
	 */
	private CFileDescriptor fd;

	/**
	 * Endere�o de IP da conex�o estabelecida.
	 */
	private InternetProtocol ip;

	/**
	 * Porta para conex�o com o servidor de mapa.
	 */
	private short port;

	/**
	 * Quantidade de personagens online no servidor.
	 */
	private short users;

	/**
	 * Vetor com o �ndice de todos os mapas dispon�veis no servidor.
	 */
	private Short maps[];

	/**
	 * Cria uma nova inst�ncia de um cliente para representa��o do servidor de mapa no servidor de personagem.
	 * Ser� necess�rio passar um arquivo descritor utilizado para realizar a conex�o entre os servidores.
	 * @param fileDecriptor refer�ncia da conex�o do servidor de personagem com o servidor de mapa.
	 */

	public ClientMapServer(CFileDescriptor fileDecriptor)
	{
		this.id = -1;
		this.fd = fileDecriptor;
		this.maps = new Short[MAX_MAP_PER_SERVER];
	}

	/**
	 * @return aquisi��o da identifica��o do servidor de mapa no servidor de personagem.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id identifica��o do servidor de mapa no servidor de personagem.
	 */

	public void setID(int id)
	{
		if (this.id == -1)
			this.id = id;
	}

	/**
	 * @return aquisi��o da conex�o do servidor de personagem com o servidor de mapa.
	 */

	public CFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	/**
	 * @return aquisi��o do endere�o de IP do servidor de mapa.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * @param serverIP endere�o de IP do servidor de mapa.
	 */

	public void setIP(InternetProtocol serverIP)
	{
		this.ip = serverIP;
	}

	/**
	 * @return aquisi��o do n�mero da porta para conex�o com o servidor de mapa.
	 */

	public short getPort()
	{
		return port;
	}

	/**
	 * @param port n�mero da porta para conex�o com o servidor de mapa.
	 */

	public void setPort(short port)
	{
		this.port = port;
	}

	/**
	 * @return aquisi��o do n�mero de personagens online no servidor de mapa.
	 */

	public short getUsers()
	{
		return users;
	}

	/**
	 * @param users n�mero de personagens online no servidor de mapa.
	 */

	public void setUsers(short users)
	{
		this.users = users;
	}

	/**
	 * @return aquisi��o do vetor contendo os mapas dispon�veis no servidor de mapas.
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

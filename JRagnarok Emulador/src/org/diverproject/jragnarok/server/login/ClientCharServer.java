package org.diverproject.jragnarok.server.login;

import static org.diverproject.util.Util.format;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.CharServerType;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Servidor de Personagem - Cliente</h1>
 *
 * <p>Usado para identificar um servidor de personagem no servidor de acesso em forma de cliente.
 * É criado somente quando o servidor de personagem solicita a conexão passando suas informações.
 * Nele também é vinculado um FD referente a sua conexão socket recebida no servidor.</p>
 *
 * <p>As informações necessárias para a criação de um cliente representando o servidor de personagem é:
 * nome do servidor (exibido ao jogador); endereço de IP e porta de acesso do servidor para que o
 * cliente do jogador poder se conectar; quantidade de jogadores online; e o tipo de servidor.</p>
 *
 * @see FileDescriptor
 * @see InternetProtocol
 * @see CharServerType
 *
 * @author Andrew
 */

public class ClientCharServer
{
	/**
	 * Código de identificação único do servidor de personagem no sistema.
	 */
	private int id;

	/**
	 * Descritor de arquivo referente a conexão no servidor de acesso.
	 */
	private FileDescriptor fd;

	/**
	 * Nome do servidor que será exibido na lista de servidores ao jogador.
	 */
	private String name;

	/**
	 * Endereço de IP para que o cliente executável do jogador se conecte ao servidor.
	 */
	private InternetProtocol ip;

	/**
	 * Porta de acesso para que o cliente executável do jogador se conecte ao servidor.
	 */
	private short port;

	/**
	 * Quantidade de jogadores online no servidor de personagem.
	 */
	private short users;

	/**
	 * Tipo do servidor de personagem.
	 */
	private CharServerType type;

	/**
	 * Determina ser o servidor de personagem é novo ou não.
	 */
	private boolean newDisplay;

	/**
	 * Cria uma nova instância de um cliente para um servidor de personagem no servidor de acesso.
	 */

	public ClientCharServer()
	{
		this.ip = new InternetProtocol();
	}

	/**
	 * @return aquisição do código de identificação único do servidor de personagem no sistema.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id código de identificação único do servidor de personagem no sistema.
	 */

	void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisição do descritor de arquivo do servidor de personagem.
	 */

	public FileDescriptor getFileDecriptor()
	{
		return fd;
	}

	/**
	 * @param fileDecriptor descritor de arquivo do servidor de personagem.
	 */

	public void setFileDecriptor(FileDescriptor fileDecriptor)
	{
		this.fd = fileDecriptor;
	}

	/**
	 * @return aquisição do nome de exibição do servidor para o jogador.
	 */

	public String getName()
	{
		return name;
	}

	/**
	 * @param name nome de exibição do servidor para o jogador.
	 */

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return aquisição do endereço de IP para conexão com o servidor.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * @return aquisição da porta de acesso para conexão com o servidor.
	 */

	public short getPort()
	{
		return port;
	}

	/**
	 * @param port porta de acesso para conexão com o servidor.
	 */

	public void setPort(short port)
	{
		this.port = port;
	}

	/**
	 * @return aquisição da quantidade de jogadores online.
	 */

	public short getUsers()
	{
		return users;
	}

	/**
	 * @param users quantidade de jogadores online.
	 */

	public void setUsers(short users)
	{
		this.users = users;
	}

	/**
	 * @return aquisição do tipo de servidor de personagem.
	 */

	public CharServerType getType()
	{
		return type;
	}

	/**
	 * @param type tipo de servidor de personagem.
	 */

	public void setType(CharServerType type)
	{
		this.type = type;
	}

	/**
	 * @return true se for um servidor novo ou false caso contrário.
	 */

	public boolean isNewDisplay()
	{
		return newDisplay;
	}

	/**
	 * @param newDisplay true se for um servidor novo ou false caso contrário.
	 */

	public void setNewDisplay(boolean newDisplay)
	{
		this.newDisplay = newDisplay;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		if (fd != null)
			description.append("fdID", fd.getID());

		description.append("serverName", name);
		description.append("addresss", format("%s:%d", ip.getString(), port));
		description.append("usersOnline", users);
		description.append("serverType", type);

		if (newDisplay)
			description.append("new-server");

		return description.toString();
	}
}

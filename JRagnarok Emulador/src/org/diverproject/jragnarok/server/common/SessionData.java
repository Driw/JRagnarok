package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.nameOf;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Dados da Sess�o</h1>
 *
 * <p>Atrav�s dos dados de uma sess�o, os servidores podem trabalhar com as conex�es.
 * Cada conex�o dever� possuir dados em sess�o conforme as escolhas do jogador.
 * Cada servidor ir� possuir um conjunto de informa��es relacionadas a suas sess�es.</p>
 *
 * @see FileDescriptor
 *
 * @author Andrew
 */

public abstract class SessionData
{
	/**
	 * C�digo de identifica��o da conta usada na sess�o.
	 */
	private int id;

	/**
	 * Tipo do cliente que solicitou a sess�o.
	 */
	private ClientType clientType;

	/**
	 * Vers�o do cliente que solicitou a sess�o.
	 */
	private int version;

	/**
	 * Objeto em cache utilizado por esse descritor.
	 */
	private Object cache;

	/**
	 * @return aquisi��o do c�digo de identifica��o da conta usada na sess�o.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id c�digo de identifica��o da conta usada na sess�o.
	 */

	public void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisi��o da vers�o do cliente que solicitou a sess�o.
	 */

	public ClientType getClientType()
	{
		return clientType;
	}

	/**
	 * @param clientType vers�o do cliente que solicitou a sess�o.
	 */

	public void setClientType(ClientType clientType)
	{
		this.clientType = clientType;
	}

	/**
	 * @return aquisi��o da vers�o do cliente que solicitou a sess�o.
	 */

	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version vers�o do cliente que solicitou a sess�o.
	 */

	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * Cache permite vincular um determinado objeto para que possa ser usado.
	 * @return aquisi��o do objeto em cache no descritor.
	 */

	public Object getCache()
	{
		return cache;
	}

	/**
	 * Cache permite vincular um determinado objeto para que possa ser usado.
	 * @param cache refer�ncia do objeto a armazenar em cache.
	 */

	public void setCache(Object cache)
	{
		this.cache = cache;
	}

	/**
	 * Procedimento interno usado para descrever as informa��es do objeto em string.
	 * @param description objeto contendo as descri��es do objeto em quest�o.
	 */

	protected abstract void toString(ObjectDescription description);

	@Override
	public final String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("clientType", clientType);
		description.append("version", version);

		if (cache != null)
			description.append("cache", nameOf(cache));

		return description.toString();
	}
}

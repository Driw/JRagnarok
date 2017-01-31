package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.nameOf;

import org.diverproject.jragnarok.server.FileDescriptor;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Dados da Sessão</h1>
 *
 * <p>Através dos dados de uma sessão, os servidores podem trabalhar com as conexões.
 * Cada conexão deverá possuir dados em sessão conforme as escolhas do jogador.
 * Cada servidor irá possuir um conjunto de informações relacionadas a suas sessões.</p>
 *
 * @see FileDescriptor
 *
 * @author Andrew
 */

public abstract class SessionData
{
	/**
	 * Código de identificação da conta usada na sessão.
	 */
	private int id;

	/**
	 * Tipo do cliente que solicitou a sessão.
	 */
	private ClientType clientType;

	/**
	 * Versão do cliente que solicitou a sessão.
	 */
	private int version;

	/**
	 * Objeto em cache utilizado por esse descritor.
	 */
	private Object cache;

	/**
	 * @return aquisição do código de identificação da conta usada na sessão.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id código de identificação da conta usada na sessão.
	 */

	public void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisição da versão do cliente que solicitou a sessão.
	 */

	public ClientType getClientType()
	{
		return clientType;
	}

	/**
	 * @param clientType versão do cliente que solicitou a sessão.
	 */

	public void setClientType(ClientType clientType)
	{
		this.clientType = clientType;
	}

	/**
	 * @return aquisição da versão do cliente que solicitou a sessão.
	 */

	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version versão do cliente que solicitou a sessão.
	 */

	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * Cache permite vincular um determinado objeto para que possa ser usado.
	 * @return aquisição do objeto em cache no descritor.
	 */

	public Object getCache()
	{
		return cache;
	}

	/**
	 * Cache permite vincular um determinado objeto para que possa ser usado.
	 * @param cache referência do objeto a armazenar em cache.
	 */

	public void setCache(Object cache)
	{
		this.cache = cache;
	}

	/**
	 * Procedimento interno usado para descrever as informações do objeto em string.
	 * @param description objeto contendo as descrições do objeto em questão.
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

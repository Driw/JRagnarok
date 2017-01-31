package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.configs.CharServerConfigs;
import org.diverproject.jragnarok.server.ServerService;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Servi�o Abstrato para Servidor de Personagem</h1>
 *
 * <p>O servidor abstrato tem apenas como objetivo facilitar a codifica��o dos servi�os.
 * Conhece todas as refer�ncias de servi�os e controles referentes ao servidor de acesso.
 * Para tal, � necess�rio que todos os servi�os sejam inicializados e destru�dos.</p>
 *
 * @see ServerService
 * @see CharServer
 *
 * @author Andrew
 */

public abstract class AbstractCharService extends ServerService
{
	/**
	 * Instancia um novo servi�o abstrato que permite ir� permitir a comunica��o entre servi�os.
	 * Todos os servi�os e controles podem ser solicitados internamente por esta classe.
	 * @param server refer�ncia do servidor de acesso que det�m este servi�o.
	 */

	public AbstractCharService(CharServer server)
	{
		super(server);
	}

	/**
	 * Inicializa os servi�os e controles do servidor de personagem definido as suas refer�ncias.
	 * As refer�ncias s�o obtidas atrav�s do servidor de personagem que det�m o servi�o.
	 */

	public abstract void init();

	/**
	 * Remover todas as refer�ncias de controles e servi�os para que o servidor possa destru�-los.
	 * Caso os servi�os n�o sejam removidos das refer�ncias podem ocupar mem�ria desnecess�ria.
	 */

	public abstract void destroy();

	@Override
	protected CharServer getServer()
	{
		return (CharServer) super.getServer();
	}

	@Override
	protected CharServerConfigs config()
	{
		return getServer().getCharServerConfigs();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("state", getServer().getState());

		return description.toString();
	}
}

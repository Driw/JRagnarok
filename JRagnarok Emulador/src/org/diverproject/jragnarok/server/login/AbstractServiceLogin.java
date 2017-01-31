package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnarok.configs.LoginServerConfigs;
import org.diverproject.jragnarok.server.ServerService;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Servi�o Abstrato para Servidor de Acesso</h1>
 *
 * <p>O servidor abstrato tem apenas como objetivo facilitar a codifica��o dos servi�os.
 * Conhece todas as refer�ncias de servi�os e controles referentes ao servidor de acesso.
 * Para tal, � necess�rio que todos os servi�os sejam inicializados e destru�dos.</p>
 *
 * @see LoginServer
 *
 * @author Andrew
 */

abstract class AbstractServiceLogin extends ServerService
{
	/**
	 * Instancia um novo servi�o abstrato que permite ir� permitir a comunica��o entre servi�os.
	 * Todos os servi�os e controles podem ser solicitados internamente por esta classe.
	 * @param server refer�ncia do servidor de acesso que det�m este servi�o.
	 */

	public AbstractServiceLogin(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa os servi�os e controles do servidor de acesso definido as suas refer�ncias.
	 * As refer�ncias s�o obtidas atrav�s do servidor de acesso que det�m o servi�o.
	 */

	public abstract void init();

	/**
	 * Remover todas as refer�ncias de controles e servi�os para que o servidor possa destru�-los.
	 * Caso os servi�os n�o sejam removidos das refer�ncias podem ocupar mem�ria desnecess�ria.
	 */

	public abstract void destroy();

	@Override
	protected LoginServer getServer()
	{
		return (LoginServer) super.getServer();
	}

	@Override
	protected LoginServerConfigs config()
	{
		return getServer().getLoginServerConfigs();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("state", getServer().getState());

		return description.toString();
	}
}

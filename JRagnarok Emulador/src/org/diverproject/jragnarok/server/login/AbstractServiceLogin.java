package org.diverproject.jragnarok.server.login;

import org.diverproject.jragnarok.configs.LoginServerConfigs;
import org.diverproject.jragnarok.server.ServerService;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Serviço Abstrato para Servidor de Acesso</h1>
 *
 * <p>O servidor abstrato tem apenas como objetivo facilitar a codificação dos serviços.
 * Conhece todas as referências de serviços e controles referentes ao servidor de acesso.
 * Para tal, é necessário que todos os serviços sejam inicializados e destruídos.</p>
 *
 * @see LoginServer
 *
 * @author Andrew
 */

abstract class AbstractServiceLogin extends ServerService
{
	/**
	 * Instancia um novo serviço abstrato que permite irá permitir a comunicação entre serviços.
	 * Todos os serviços e controles podem ser solicitados internamente por esta classe.
	 * @param server referência do servidor de acesso que detém este serviço.
	 */

	public AbstractServiceLogin(LoginServer server)
	{
		super(server);
	}

	/**
	 * Inicializa os serviços e controles do servidor de acesso definido as suas referências.
	 * As referências são obtidas através do servidor de acesso que detém o serviço.
	 */

	public abstract void init();

	/**
	 * Remover todas as referências de controles e serviços para que o servidor possa destruí-los.
	 * Caso os serviços não sejam removidos das referências podem ocupar memória desnecessária.
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

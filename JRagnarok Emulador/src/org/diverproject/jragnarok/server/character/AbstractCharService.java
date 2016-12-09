package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.server.ServerService;
import org.diverproject.jragnarok.server.character.control.AuthControl;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.character.control.OnlineCharControl;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Serviço Abstrato para Servidor de Personagem</h1>
 *
 * <p>O servidor abstrato tem apenas como objetivo facilitar a codificação dos serviços.
 * Conhece todas as referências de serviços e controles referentes ao servidor de acesso.
 * Para tal, é necessário que todos os serviços sejam inicializados e destruídos.</p>
 *
 * @see ServiceCharClient
 * @see ServiceCharLogin
 * @see ServiceCharServer
 * @see ServiceCharServerAuth
 * @see AuthControl
 * @see CharacterControl
 * @see OnlineCharControl
 *
 * @author Andrew
 */

public class AbstractCharService extends ServerService
{
	/**
	 * Serviço para comunicação inicial com o cliente.
	 */
	protected ServiceCharClient client;

	/**
	 * Serviço principal do servidor de personagem.
	 */
	protected ServiceCharServer character;

	/**
	 * Serviço para comunicação com o servidor de acesso.
	 */
	protected ServiceCharLogin login;

	/**
	 * Serviço para comunicação com o servidor de mapa.
	 */
	protected ServiceCharMap map;

	/**
	 * Serviço para autenticação dos jogadores no servidor.
	 */
	protected ServiceCharServerAuth auth;


	/**
	 * Controle para autenticação de jogadores online.
	 */
	protected AuthControl auths;

	/**
	 * Controle para dados de personagens online.
	 */
	protected OnlineCharControl onlines;

	/**
	 * Controle para gerenciar dados dos personagens.
	 */
	protected CharacterControl characters;

	/**
	 * Instancia um novo serviço abstrato que permite irá permitir a comunicação entre serviços.
	 * Todos os serviços e controles podem ser solicitados internamente por esta classe.
	 * @param server referência do servidor de acesso que detém este serviço.
	 */

	public AbstractCharService(CharServer server)
	{
		super(server);
	}

	/**
	 * Inicializa os serviços e controles do servidor de personagem definido as suas referências.
	 * As referências são obtidas através do servidor de personagem que detém o serviço.
	 */

	public void init()
	{
		client = getServer().getCharClient();
		character = getServer().getCharServer();
		login = getServer().getCharLogin();
		auth = getServer().getCharServerAuth();

		auths = getServer().getAuthControl();
		onlines = getServer().getOnlineCharControl();
		characters = getServer().getCharacterControl();
	}

	/**
	 * Remover todas as referências de controles e serviços para que o servidor possa destruí-los.
	 * Caso os serviços não sejam removidos das referências podem ocupar memória desnecessária.
	 */

	public void destroy()
	{
		client = null;
		character = null;
		login = null;
		auth = null;

		auths = null;
		onlines = null;
		characters = null;
	}

	@Override
	protected CharServer getServer()
	{
		return (CharServer) super.getServer();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("state", getServer().getState());

		return description.toString();
	}
}

package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.server.ServerService;
import org.diverproject.jragnarok.server.character.control.AuthControl;
import org.diverproject.jragnarok.server.character.control.CharacterControl;
import org.diverproject.jragnarok.server.character.control.OnlineCharControl;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Servi�o Abstrato para Servidor de Personagem</h1>
 *
 * <p>O servidor abstrato tem apenas como objetivo facilitar a codifica��o dos servi�os.
 * Conhece todas as refer�ncias de servi�os e controles referentes ao servidor de acesso.
 * Para tal, � necess�rio que todos os servi�os sejam inicializados e destru�dos.</p>
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
	 * Servi�o para comunica��o inicial com o cliente.
	 */
	protected ServiceCharClient client;

	/**
	 * Servi�o principal do servidor de personagem.
	 */
	protected ServiceCharServer character;

	/**
	 * Servi�o para comunica��o com o servidor de acesso.
	 */
	protected ServiceCharLogin login;

	/**
	 * Servi�o para comunica��o com o servidor de mapa.
	 */
	protected ServiceCharMap map;

	/**
	 * Servi�o para autentica��o dos jogadores no servidor.
	 */
	protected ServiceCharServerAuth auth;


	/**
	 * Controle para autentica��o de jogadores online.
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
	 * Remover todas as refer�ncias de controles e servi�os para que o servidor possa destru�-los.
	 * Caso os servi�os n�o sejam removidos das refer�ncias podem ocupar mem�ria desnecess�ria.
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

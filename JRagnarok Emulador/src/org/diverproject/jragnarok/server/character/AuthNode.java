package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.util.Time;

/**
 * <h1>Nó de Autenticação</h1>
 *
 * <p>Um nó de autenticação é usado no servidor de personagem para garantir a autenticação de uma conta.
 * Quando a autenticação do usuário e personagem passados pelo jogador são validados o nó é registrado.</p>
 *
 * <p>Assim que o jogador entrar no servidor de mapas ele irá solicitar esse nó de autenticação.
 * Após a solicitação feita, o nó é removido do servidor de personagem repassando as informações ao
 * servidor de mapa, concluindo portanto a autenticação completa do acesso de um personagem no sistema.</p>
 *
 * <p>Para este nó será necessário identificar a conta, personagem e informações básicas da conta.
 * As informações são de: seed gerado pelo servidor de acesso (primeira e segunda seed); endereço de IP;
 * versão do cliente e tipo de cliente que são fornecidos somente ao servidor de acesso e grupo.</p>
 *
 * @see LoginSeed
 * @see InternetProtocol
 * @see Group
 *
 * @author Andrew
 */

public class AuthNode
{
	/**
	 * Código de identificação da conta.
	 */
	private int accountID;

	/**
	 * Código de identificação do personagem selecionado (0: nenhum).
	 */
	private int charID;

	/**
	 * Seed que garantir que a sessão não receba informações incorretas.
	 */
	private LoginSeed seed;

	/**
	 * Endereço de IP do qual o jogador está fazendo o acesso.
	 */
	private InternetProtocol ip;

	/**
	 * Horário em que a conta terá seu acesso expirado no sistema.
	 */
	private Time expiration;

	/**
	 * Grupo de contas em que se encontra a conta do jogador.
	 */
	private Group group;

	/**
	 * TODO ???
	 */
	private int changingMapServers;

	/**
	 * Número da versão do cliente para diferenciar os dados dos pacotes.
	 */
	private int version;

	/**
	 * Cria uma nova instância para um nó de autenticação usado no servidor de personagem.
	 * Inicializa o objeto para armazenar o horário de expiração e endereço de IP.
	 */

	public AuthNode()
	{
		changingMapServers = 1;

		expiration = new Time();
		ip = new InternetProtocol();
	}

	/**
	 * @return aquisição do código de identificação da conta do jogador.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @param accountID código de identificação da conta do jogador.
	 */

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	/**
	 * @return aquisição do código de identificação do personagem selecionado.
	 */

	public int getCharID()
	{
		return charID;
	}

	/**
	 * @param charID código de identificação do personagem selecionado.
	 */

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	/**
	 * @return aquisição da seed de acesso que garantir que a sessão não receba informações incorretas.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @param seed seed de acesso que garantir que a sessão não receba informações incorretas.
	 */

	public void setSeed(LoginSeed seed)
	{
		this.seed = seed;
	}

	/**
	 * @return aquisição do endereço de IP do qual o jogador está fazendo o acesso.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * @return aquisição do horário em que a conta terá seu acesso expirado no sistema.
	 */

	public Time getExpiration()
	{
		return expiration;
	}

	/**
	 * @return aquisição do grupo de contas em que se encontra a conta do jogador.
	 */

	public Group getGroup()
	{
		return group;
	}

	/**
	 * @param group grupo de contas em que se encontra a conta do jogador.
	 */

	public void setGroup(Group group)
	{
		this.group = group;
	}

	/**
	 * @return ???
	 */

	public int getChangingMapServers()
	{
		return changingMapServers;
	}

	/**
	 * @param changingMapServers ???
	 */

	public void setChangingMapServers(int changingMapServers)
	{
		this.changingMapServers = changingMapServers;
	}

	/**
	 * @return aquisição do número da versão do cliente para diferenciar os dados dos pacotes.
	 */

	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version número da versão do cliente para diferenciar os dados dos pacotes.
	 */

	public void setVersion(int version)
	{
		this.version = version;
	}
}

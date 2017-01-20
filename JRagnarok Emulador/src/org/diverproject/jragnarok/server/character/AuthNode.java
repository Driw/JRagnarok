package org.diverproject.jragnarok.server.character;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.util.Time;

/**
 * <h1>N� de Autentica��o</h1>
 *
 * <p>Um n� de autentica��o � usado no servidor de personagem para garantir a autentica��o de uma conta.
 * Quando a autentica��o do usu�rio e personagem passados pelo jogador s�o validados o n� � registrado.</p>
 *
 * <p>Assim que o jogador entrar no servidor de mapas ele ir� solicitar esse n� de autentica��o.
 * Ap�s a solicita��o feita, o n� � removido do servidor de personagem repassando as informa��es ao
 * servidor de mapa, concluindo portanto a autentica��o completa do acesso de um personagem no sistema.</p>
 *
 * <p>Para este n� ser� necess�rio identificar a conta, personagem e informa��es b�sicas da conta.
 * As informa��es s�o de: seed gerado pelo servidor de acesso (primeira e segunda seed); endere�o de IP;
 * vers�o do cliente e tipo de cliente que s�o fornecidos somente ao servidor de acesso e grupo.</p>
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
	 * C�digo de identifica��o da conta.
	 */
	private int accountID;

	/**
	 * C�digo de identifica��o do personagem selecionado (0: nenhum).
	 */
	private int charID;

	/**
	 * Seed que garantir que a sess�o n�o receba informa��es incorretas.
	 */
	private LoginSeed seed;

	/**
	 * Endere�o de IP do qual o jogador est� fazendo o acesso.
	 */
	private InternetProtocol ip;

	/**
	 * Hor�rio em que a conta ter� seu acesso expirado no sistema.
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
	 * N�mero da vers�o do cliente para diferenciar os dados dos pacotes.
	 */
	private int version;

	/**
	 * Cria uma nova inst�ncia para um n� de autentica��o usado no servidor de personagem.
	 * Inicializa o objeto para armazenar o hor�rio de expira��o e endere�o de IP.
	 */

	public AuthNode()
	{
		changingMapServers = 1;

		expiration = new Time();
		ip = new InternetProtocol();
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o da conta do jogador.
	 */

	public int getAccountID()
	{
		return accountID;
	}

	/**
	 * @param accountID c�digo de identifica��o da conta do jogador.
	 */

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do personagem selecionado.
	 */

	public int getCharID()
	{
		return charID;
	}

	/**
	 * @param charID c�digo de identifica��o do personagem selecionado.
	 */

	public void setCharID(int charID)
	{
		this.charID = charID;
	}

	/**
	 * @return aquisi��o da seed de acesso que garantir que a sess�o n�o receba informa��es incorretas.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @param seed seed de acesso que garantir que a sess�o n�o receba informa��es incorretas.
	 */

	public void setSeed(LoginSeed seed)
	{
		this.seed = seed;
	}

	/**
	 * @return aquisi��o do endere�o de IP do qual o jogador est� fazendo o acesso.
	 */

	public InternetProtocol getIP()
	{
		return ip;
	}

	/**
	 * @return aquisi��o do hor�rio em que a conta ter� seu acesso expirado no sistema.
	 */

	public Time getExpiration()
	{
		return expiration;
	}

	/**
	 * @return aquisi��o do grupo de contas em que se encontra a conta do jogador.
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
	 * @return aquisi��o do n�mero da vers�o do cliente para diferenciar os dados dos pacotes.
	 */

	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version n�mero da vers�o do cliente para diferenciar os dados dos pacotes.
	 */

	public void setVersion(int version)
	{
		this.version = version;
	}
}

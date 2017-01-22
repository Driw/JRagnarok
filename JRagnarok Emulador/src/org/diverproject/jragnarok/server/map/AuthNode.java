package org.diverproject.jragnarok.server.map;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import org.diverproject.jragnarok.server.InternetProtocol;
import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.common.Sex;
import org.diverproject.jragnarok.server.common.entities.Character;
import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

/**
 * <h1>N� de Autentica��o</h1>
 *
 * <p>Um n� de autentica��o � usado no servidor de mapa para garantir a autentica��o de uma conta.
 * Quando a autentica��o do usu�rio e personagem selecionado pelo jogador s�o validados o n� � registrado.</p>
 *
 * <p>Assim que o jogador entrar no servidor de mapas ser� feita a autentica��o e este n� garante isso.
 * Ap�s realizar a autentica��o no servidor de mapas o servidor de personagem dever� remover a sua.
 * Assim ir� manter a conta conectada somente ao servidor de mapa at� que volte para a sele��o de personagens.</p>
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
	 * Descritor de Arquivo referente a conex�o de um cliente com o servidor de mapa.
	 */
	private MFileDescriptor fd;

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
	 * Hor�rio em que a conta ter� seu acesso expirado no sistema.
	 */
	private Time expiration;

	/**
	 * Sexo do jogador especificado em sua conta.
	 */
	private Sex sex;

	/**
	 * Entidade contendo as informa��es do personagem selecionado para jogar.
	 */
	private Character character;

	/**
	 * Hor�rio em que o n� foi criado no servidor de mapas.
	 */
	private Time creationTime;

	/**
	 * Estado do personagem (jogador) no servidor de mapas.
	 */
	private PlayerState state;

	/**
	 * Cria uma nova inst�ncia para um n� de autentica��o usado no servidor de personagem.
	 * Inicializa o objeto para armazenar o hor�rio de expira��o e endere�o de IP.
	 */

	public AuthNode()
	{
		expiration = new Time();
		creationTime = new Time(System.currentTimeMillis());
	}

	/**
	 * @return aquisi��o da conex�o de um cliente com o servidor de mapa.
	 */

	public MFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	/**
	 * @param fd conex�o de um cliente com o servidor de mapa.
	 */

	public void setFileDescriptor(MFileDescriptor fd)
	{
		this.fd = fd;
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
	 * @return aquisi��o do hor�rio em que a conta ter� seu acesso expirado no sistema.
	 */

	public Time getExpiration()
	{
		return expiration;
	}

	/**
	 * @return sexo do jogador especificado em sua conta.
	 */

	public Sex getSex()
	{
		return sex;
	}

	/**
	 * @param sex sexo do jogador especificado em sua conta.
	 */

	public void setSex(Sex sex)
	{
		this.sex = sex;
	}

	/**
	 * @return entidade contendo as informa��es do personagem selecionado.
	 */

	public Character getCharacter()
	{
		return character;
	}

	/**
	 * @param character entidade contendo as informa��es do personagem selecionado.
	 */

	public void setCharacter(Character character)
	{
		this.character = character;
	}

	/**
	 * @return estado do personagem no servidor de mapa.
	 */

	public PlayerState getState()
	{
		return state;
	}

	/**
	 * @param state estado do personagem no servidor de mapa.
	 */

	public void setState(PlayerState state)
	{
		this.state = state;
	}

	/**
	 * @return aquisi��o do hor�rio da cria��o do n�.
	 */

	public Time getCreationTime()
	{
		return creationTime;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("accountID", accountID);
		description.append("charID", charID);
		description.append("seed", format("%d:%d", seed == null ? 0 : seed.getFirst(), seed == null ? 0 : seed.getSecond()));
		description.append("expiration", expiration);
		description.append("sex", sex);
		description.append("charid", character == null ? 0 : character.getID());
		description.append("creationTime", creationTime);

		return description.toString();
	}
}

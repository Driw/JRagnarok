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
 * <h1>Nó de Autenticação</h1>
 *
 * <p>Um nó de autenticação é usado no servidor de mapa para garantir a autenticação de uma conta.
 * Quando a autenticação do usuário e personagem selecionado pelo jogador são validados o nó é registrado.</p>
 *
 * <p>Assim que o jogador entrar no servidor de mapas será feita a autenticação e este nó garante isso.
 * Após realizar a autenticação no servidor de mapas o servidor de personagem deverá remover a sua.
 * Assim irá manter a conta conectada somente ao servidor de mapa até que volte para a seleção de personagens.</p>
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
	 * Descritor de Arquivo referente a conexão de um cliente com o servidor de mapa.
	 */
	private MFileDescriptor fd;

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
	 * Horário em que a conta terá seu acesso expirado no sistema.
	 */
	private Time expiration;

	/**
	 * Sexo do jogador especificado em sua conta.
	 */
	private Sex sex;

	/**
	 * Entidade contendo as informações do personagem selecionado para jogar.
	 */
	private Character character;

	/**
	 * Horário em que o nó foi criado no servidor de mapas.
	 */
	private Time creationTime;

	/**
	 * Estado do personagem (jogador) no servidor de mapas.
	 */
	private PlayerState state;

	/**
	 * Cria uma nova instância para um nó de autenticação usado no servidor de personagem.
	 * Inicializa o objeto para armazenar o horário de expiração e endereço de IP.
	 */

	public AuthNode()
	{
		expiration = new Time();
		creationTime = new Time(System.currentTimeMillis());
	}

	/**
	 * @return aquisição da conexão de um cliente com o servidor de mapa.
	 */

	public MFileDescriptor getFileDescriptor()
	{
		return fd;
	}

	/**
	 * @param fd conexão de um cliente com o servidor de mapa.
	 */

	public void setFileDescriptor(MFileDescriptor fd)
	{
		this.fd = fd;
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
	 * @return aquisição do horário em que a conta terá seu acesso expirado no sistema.
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
	 * @return entidade contendo as informações do personagem selecionado.
	 */

	public Character getCharacter()
	{
		return character;
	}

	/**
	 * @param character entidade contendo as informações do personagem selecionado.
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
	 * @return aquisição do horário da criação do nó.
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

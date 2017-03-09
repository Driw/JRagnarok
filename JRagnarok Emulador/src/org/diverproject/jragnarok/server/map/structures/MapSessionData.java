package org.diverproject.jragnarok.server.map.structures;

import static org.diverproject.jragnarok.JRagnarokConstants.PASSWORD_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.USERNAME_LENGTH;
import static org.diverproject.util.Util.format;
import static org.diverproject.util.Util.strcap;

import org.diverproject.jragnarok.server.common.LoginSeed;
import org.diverproject.jragnarok.server.common.SessionData;
import org.diverproject.jragnarok.server.common.entities.Character;
import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

/**
 * <h1>Dados da Sess�o de Mapa</h1>
 *
 * <p>Os dados desse tipo de sess�o s�o definidos para serem utilizados no servidor de mapa.
 * Dever�o conter informa��es informadas pelo jogador e cliente execut�vel do qual usou.
 * Algumas outras informa��es ser�o definidas ap�s o servidor de personagem autorizar.</p>
 *
 * <p>As informa��es que s�o definidas no servidor s�o: hor�rio de expira��o da conta,
 * seed do acesso, dados do personagem e identifica��o do mapa.
 * As informa��es obtidas do jogador s�o: usu�rio, senha, idioma e vers�o do cliente.</p>
 *
 * @see SessionData
 * @see Time
 * @see Group
 * @see LoginSeed
 *
 * @author Andrew
 */

public class MapSessionData extends SessionData
{
	/**
	 * Nome de usu�rio da conta do jogador.
	 */
	private String username;

	/**
	 * Senha respectiva ao nome de usu�rio.
	 */
	private String password;

	/**
	 * Hor�rio de expira��o da conta.
	 */
	private Time expiration;

	/**
	 * Informa��es do grupo de contas do jogador.
	 */
	private Group group;

	/**
	 * Seed para valida��o das informa��es dessa sess�o.
	 */
	private LoginSeed seed;

	/**
	 * Tipo de idioma do cliente.
	 */
	private int langType;

	/**
	 * N�mero da vers�o do cliente.
	 */
	private int version;

	/**
	 * Dados do personagem selecionado pelo jogador.
	 */
	private Character character;

	/**
	 * Identifica��o do mapa em que o personagem est� alocado.
	 */
	private int mapIndex;

	/**
	 * Chave para criptografia dos pacotes.
	 */
	private int cryptKey;

	/**
	 * Cria uma nova inst�ncia de dados para uma sess�o no servidor de mapas.
	 * Inicializa a seed e o hor�rio de expira��o para que sejam utilizados.
	 */

	public MapSessionData()
	{
		seed = new LoginSeed();
		expiration = new Time();
	}

	/**
	 * @return aquisi��o do nome de usu�rio da conta do jogador.
	 */

	public String getUsername()
	{
		return username;
	}

	/**
	 * @param username nome de usu�rio da conta do jogador.
	 */

	public void setUsername(String username)
	{
		this.username = strcap(username, USERNAME_LENGTH);
	}

	/**
	 * @return aquisi��o da senha referente ao nome de usu�rio.
	 */

	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password senha respectiva a conta do jogador.
	 */

	public void setPassword(String password)
	{
		this.password = strcap(password, PASSWORD_LENGTH);
	}

	/**
	 * @return aquisi��o da grupo de contas do jogador.
	 */

	public Group getGroup()
	{
		return group;
	}

	/**
	 * @param group grupo de contas do jogador.
	 */

	public void setGroup(Group group)
	{
		this.group = group;
	}

	/**
	 * @return aquisi��o do hor�rio de expira��o da conta.
	 */

	public Time getExpiration()
	{
		return expiration;
	}

	/**
	 * @return aquisi��o da seed para valida��o da sess�o.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @return aquisi��o 
	 */

	public int getLangType()
	{
		return langType;
	}

	public void setLangType(int langType)
	{
		this.langType = langType;
	}

	/**
	 * @return aquisi��o do n�mero da vers�o do cliente.
	 */

	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version n�mero da vers�o do cliente.
	 */

	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * @return aquisi��o dos dados do personagem selecionado pelo jogador.
	 */

	public Character getCharacter()
	{
		return character;
	}

	/**
	 * @param character dados do personagem selecionado pelo jogador.
	 */

	public void setCharacter(Character character)
	{
		this.character = character;
	}

	/**
	 * @return aquisi��o da identifica��o do mapa em que o personagem est� alocado.
	 */

	public int getMapIndex()
	{
		return mapIndex;
	}

	/**
	 * @param mapIndex identifica��o do mapa em que o personagem est� alocado.
	 */

	public void setMapIndex(int mapIndex)
	{
		this.mapIndex = mapIndex;
	}

	/**
	 * @return aquisi��o da chave de criptografia de pacotes.
	 */

	public int getCryptKey()
	{
		return cryptKey;
	}

	/**
	 * @param cryptKey chave de criptografia de pacotes.
	 */

	public void setCryptKey(int cryptKey)
	{
		this.cryptKey = cryptKey;
	}

	@Override
	protected void toString(ObjectDescription description)
	{
		description.append("username", username);
		description.append("password", password);
		description.append("expiration", expiration);
		description.append("group", group);
		description.append("seed", format("%d %d", seed.getFirst(), seed.getSecond()));
		description.append("langType", langType);
		description.append("version", version);
		description.append("mapIndex", mapIndex);
		description.append("charID", character != null ? character.getID() : 0);
	}
}

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
 * <h1>Dados da Sessão de Mapa</h1>
 *
 * <p>Os dados desse tipo de sessão são definidos para serem utilizados no servidor de mapa.
 * Deverão conter informações informadas pelo jogador e cliente executável do qual usou.
 * Algumas outras informações serão definidas após o servidor de personagem autorizar.</p>
 *
 * <p>As informações que são definidas no servidor são: horário de expiração da conta,
 * seed do acesso, dados do personagem e identificação do mapa.
 * As informações obtidas do jogador são: usuário, senha, idioma e versão do cliente.</p>
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
	 * Nome de usuário da conta do jogador.
	 */
	private String username;

	/**
	 * Senha respectiva ao nome de usuário.
	 */
	private String password;

	/**
	 * Horário de expiração da conta.
	 */
	private Time expiration;

	/**
	 * Informações do grupo de contas do jogador.
	 */
	private Group group;

	/**
	 * Seed para validação das informações dessa sessão.
	 */
	private LoginSeed seed;

	/**
	 * Tipo de idioma do cliente.
	 */
	private int langType;

	/**
	 * Número da versão do cliente.
	 */
	private int version;

	/**
	 * Dados do personagem selecionado pelo jogador.
	 */
	private Character character;

	/**
	 * Identificação do mapa em que o personagem está alocado.
	 */
	private int mapIndex;

	/**
	 * Chave para criptografia dos pacotes.
	 */
	private int cryptKey;

	/**
	 * Cria uma nova instância de dados para uma sessão no servidor de mapas.
	 * Inicializa a seed e o horário de expiração para que sejam utilizados.
	 */

	public MapSessionData()
	{
		seed = new LoginSeed();
		expiration = new Time();
	}

	/**
	 * @return aquisição do nome de usuário da conta do jogador.
	 */

	public String getUsername()
	{
		return username;
	}

	/**
	 * @param username nome de usuário da conta do jogador.
	 */

	public void setUsername(String username)
	{
		this.username = strcap(username, USERNAME_LENGTH);
	}

	/**
	 * @return aquisição da senha referente ao nome de usuário.
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
	 * @return aquisição da grupo de contas do jogador.
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
	 * @return aquisição do horário de expiração da conta.
	 */

	public Time getExpiration()
	{
		return expiration;
	}

	/**
	 * @return aquisição da seed para validação da sessão.
	 */

	public LoginSeed getSeed()
	{
		return seed;
	}

	/**
	 * @return aquisição 
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
	 * @return aquisição do número da versão do cliente.
	 */

	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version número da versão do cliente.
	 */

	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * @return aquisição dos dados do personagem selecionado pelo jogador.
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
	 * @return aquisição da identificação do mapa em que o personagem está alocado.
	 */

	public int getMapIndex()
	{
		return mapIndex;
	}

	/**
	 * @param mapIndex identificação do mapa em que o personagem está alocado.
	 */

	public void setMapIndex(int mapIndex)
	{
		this.mapIndex = mapIndex;
	}

	/**
	 * @return aquisição da chave de criptografia de pacotes.
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

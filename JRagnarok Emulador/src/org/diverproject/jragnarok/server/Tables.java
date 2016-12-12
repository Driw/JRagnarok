package org.diverproject.jragnarok.server;

/**
 * <h1>Tabelas</h1>
 *
 * <p>Classe que possui o nome de todas as tabelas do banco de dados.
 * Assim � poss�vel alterar o nome de uma tabela de forma mais simples no sistema.
 * Basta alterar atrav�s de um setter o nome da tabela e ser� aplicado ao sistema.
 * Para que essa classe funcione de fato as queries devem usar essa classe.</p>
 *
 * @author Andrew
 */

public class Tables
{
	/**
	 * �nica inst�ncia dispon�vel de Tabelas.
	 */
	private static final Tables INSTANCE = new Tables();

	/**
	 * Tabelas utiliza o Padr�o de Projetos Singleton.
	 * Por esse motivo � necess�rio a exist�ncia desse m�todo para obt�-lo.
	 * @return aquisi��o da �nica inst�ncia de Tabelas.
	 */

	public static Tables getInstance()
	{
		return INSTANCE;
	}


	/**
	 * Tabela contendo os registros do acesso de contas.
	 */
	private String loginLog;

	/**
	 * Tabela contendo os detalhes das contas dos jogadores.
	 */
	private String accounts;

	/**
	 * Tabela contendo os detalhes de c�digo pin dos jogadores.
	 */
	private String pincodes;

	/**
	 * Tabela contendo o agrupamento dos jogadores.
	 */
	private String accountsGroup;

	/**
	 * Tabela contendo as propriedades dos agrupamentos de jogadores.
	 */
	private String groups;

	/**
	 * Tabela contendo todos os comandos para jogadores.
	 */
	private String groupsCommands;

	/**
	 * Tabela contendo todas as permiss�es para jogadores.
	 */
	private String groupsPermissions;

	/**
	 * Tabela contendo as configura��es dos acessos vip.
	 */
	private String vips;

	/**
	 * Tabela contendo a lista de endere�os IP banidos.
	 */
	private String ipBan;

	/**
	 * Tabela contendo a vincula��o dos personagens com as contas.
	 */
	private String accountsCharacters;

	/**
	 * Tabela contendo todos os dados b�sicos dos personagens.
	 */
	private String characters;

	/**
	 * Tabela contendo os atributos b�sicos distribu�dos nos personagens.
	 */
	private String charStats;

	/**
	 * Tabela contendo todas da apar�ncia atual dos personagens.
	 */
	private String charLook;

	/**
	 * Tabela contendo todas as experi�ncias obtidas pelos personagens.
	 */
	private String charExperiences;

	/**
	 * Tabela contendo dados de fam�lia dos personagens.
	 */
	private String charFamily;

	/**
	 * Tabela contendo dados das localiza��es salvas dos personagens.
	 */
	private String charLocations;

	/**
	 * Tabela contendo dados de classifica��o de assistentes dos personagens.
	 */
	private String charMercenaryRank;

	/**
	 * Construtor privado para aderir ao Padr�o de Projetos: Singleton.
	 * Define o nome padr�o de todas as tabelas do banco de dados.
	 */

	private Tables()
	{
		loginLog = "login_log";

		accounts = "accounts";
		accountsCharacters = "accounts_char_list";
		accountsGroup = "accounts_groups";
		pincodes = "pincodes";

		groups = "groups";
		groupsPermissions = "groups_permissions";
		groupsCommands = "groups_commands";

		vips = "vips";

		ipBan = "ipban_list";

		characters = "characters";
		charStats = "characters_stats";
		charLook = "characters_look";
		charExperiences = "characters_exp";
		charFamily = "characters_family";
		charLocations = "characters_locations";
		charMercenaryRank = "characters_mercenary_rank";
	}

	/**
	 * @return aquisi��o do nome da tabela que registra o acesso de contas.
	 */

	public String getLoginLog()
	{
		return loginLog;
	}

	/**
	 * @param loginLog nome da tabela que registra o acesso de contas.
	 */

	public void setLoginLog(String loginLog)
	{
		this.loginLog = loginLog;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo informa��es das contas dos jogadores.
	 */

	public String getAccounts()
	{
		return accounts;
	}

	/**
	 * @param account nome da tabela contendo informa��es das contas dos jogadores.
	 */

	public void setAccounts(String account)
	{
		this.accounts = account;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo os dados b�sicos dos personagens.
	 */

	public String getCharacters()
	{
		return characters;
	}

	/**
	 * @param ipBan nome da tabela contendo os dados b�sicos dos personagens.
	 */

	public void setCharacters(String characters)
	{
		this.characters = characters;
	}

	/**
	 * @return aquisi��o do nome da tabela com detalhes do c�digo pin dos jogadores.
	 */

	public String getPincodes()
	{
		return pincodes;
	}

	/**
	 * @param pincode nome da tabela com detalhes do c�digo pin dos jogadores.
	 */

	public void setPincodes(String pincode)
	{
		this.pincodes = pincode;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo o agrupamento dos jogadores.
	 */

	public String getAccountsGroup()
	{
		return accountsGroup;
	}

	/**
	 * @param accountGroup nome da tabela contendo o agrupamento dos jogadores.
	 */

	public void setAccountsGroup(String accountGroup)
	{
		this.accountsGroup = accountGroup;
	}

	/**
	 * @return aquisi��o do nome da tabela com propriedades dos agrupamentos de jogadores.
	 */

	public String getGroups()
	{
		return groups;
	}

	/**
	 * @param group nome da tabela com propriedades dos agrupamentos de jogadores.
	 */

	public void setGroups(String group)
	{
		this.groups = group;
	}

	/**
	 * @return aquisi��o do nome da tabela com os comandos dispon�veis para grupos.
	 */

	public String getGroupsCommands()
	{
		return groupsCommands;
	}

	/**
	 * @param groupsCommands nome da tabela com os comandos dispon�veis para grupos.
	 */

	public void setGroupsCommands(String groupsCommands)
	{
		this.groupsCommands = groupsCommands;
	}

	/**
	 * @return aquisi��o do nome da tabela com as permiss�es dispon�veis para grupos.
	 */

	public String getGroupsPermissions()
	{
		return groupsPermissions;
	}

	/**
	 * @param groupsPermissions nome da tabela com as permiss�es dispon�veis para grupos.
	 */

	public void setGroupsPermissions(String groupsPermissions)
	{
		this.groupsPermissions = groupsPermissions;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo as configura��es dos acessos vip.
	 */

	public String getVips()
	{
		return vips;
	}

	/**
	 * @param vips nome da tabela contendo as configura��es dos acessos vip.
	 */

	public void setVips(String vips)
	{
		this.vips = vips;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo a lista de endere�os IP banidos.
	 */

	public String getIpBan()
	{
		return this.ipBan;
	}

	/**
	 * @param ipBan nome da tabela contendo a lista de endere�os IP banidos.
	 */

	public void setIpBan(String ipBan)
	{
		this.ipBan = ipBan;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo a vincula��o dos personagens com as contas.
	 */

	public String getAccountsCharacters()
	{
		return accountsCharacters;
	}

	/**
	 * @param accountCharacters tabela contendo a vincula��o dos personagens com as contas.
	 */

	public void setAccountsCharacters(String accountCharacters)
	{
		this.accountsCharacters = accountCharacters;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo os atributos b�sicos distribu�dos nos personagens.
	 */

	public String getCharStats()
	{
		return charStats;
	}

	/**
	 * @param charStats nome da tabela contendo os atributos b�sicos distribu�dos nos personagens.
	 */

	public void setCharStats(String charStats)
	{
		this.charStats = charStats;
	}

	/**
	 * @return aquisi��o da tabela contendo todas da apar�ncia atual dos personagens.
	 */

	public String getCharLook()
	{
		return charLook;
	}

	/**
	 * @param charLook tabela contendo todas da apar�ncia atual dos personagens.
	 */

	public void setCharLook(String charLook)
	{
		this.charLook = charLook;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo a experi�ncias obtidas pelos personagens.
	 */

	public String getCharExperience()
	{
		return charExperiences;
	}

	/**
	 * @param experiences nome da tabela contendo a experi�ncias obtidas pelos personagens.
	 */

	public void setCharExperiences(String experiences)
	{
		this.charExperiences = experiences;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo dados de fam�lia dos personagens.
	 */

	public String getCharFamily()
	{
		return charFamily;
	}

	/**
	 * @param charFamily nome da tabela contendo dados de fam�lia dos personagens.
	 */

	public void setCharFamily(String charFamily)
	{
		this.charFamily = charFamily;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo dados das localiza��es salvas personagens.
	 */

	public String getCharLocations()
	{
		return charLocations;
	}

	/**
	 * @param charLocations nome da tabela contendo dados das localiza��es salvas personagens.
	 */

	public void setCharLocations(String charLocations)
	{
		this.charLocations = charLocations;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo dados de classifica��o de assistentes dos personagens.
	 */

	public String getCharMercenaryRank()
	{
		return charMercenaryRank;
	}

	/**
	 * @param charMercenaryRank nome da tabela contendo dados de classifica��o de assistentes dos personagens.
	 */

	public void setCharMercenaryRank(String charMercenaryRank)
	{
		this.charMercenaryRank = charMercenaryRank;
	}
}

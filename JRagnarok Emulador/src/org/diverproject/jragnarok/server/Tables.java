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
	 * Tabela contendo todos os comandos habilitados dos grupos.
	 */
	private String groupCommandsList;

	/**
	 * Tabela contendo todas as permiss�es habilitadas dos grupos.
	 */
	private String groupPermissionsList;

	/**
	 * Tabela contendo todos os comandos para jogadores.
	 */
	private String groupCommands;

	/**
	 * Tabela contendo todas as permiss�es para jogadores.
	 */
	private String groupPermissions;

	/**
	 * Tabela contendo as configura��es dos acessos vip.
	 */
	private String vip;

	/**
	 * Tabela contendo a lista de endere�os IP banidos.
	 */
	private String ipBan;

	/**
	 * Tabela contendo todos os dados b�sicos dos personagens.
	 */
	private String characters;

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
		accountsGroup = "accounts_groups";
		pincodes = "pincodes";

		groups = "groups";
		groupPermissionsList = "group_permissions_list";
		groupCommandsList = "group_commands_list";
		groupPermissions = "group_permissions";
		groupCommands = "group_commands";

		vip = "vip";

		ipBan = "ipban_list";

		characters = "characters";
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
	 * @return aquisi��o do nome da tabela contendo todos os comandos habilitados dos grupos.
	 */

	public String getGroupCommandsList()
	{
		return groupCommandsList;
	}

	/**
	 * @param groupCommands nome da tabela contendo todos os comandos habilitados dos grupos.
	 */

	public void setGroupCommandsList(String groupCommands)
	{
		this.groupCommandsList = groupCommands;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo todas as permiss�es habilitados dos grupos.
	 */

	public String getGroupPermissionsList()
	{
		return groupPermissionsList;
	}

	/**
	 * @param groupPermissions nome da tabela contendo todas as permiss�es habilitados dos grupos.
	 */

	public void setGroupPermissionsList(String groupPermissions)
	{
		this.groupPermissionsList = groupPermissions;
	}

	/**
	 * @return aquisi��o do nome da tabela com os comandos dispon�veis para grupos.
	 */

	public String getGroupCommands()
	{
		return groupCommands;
	}

	/**
	 * @param groupCommands nome da tabela com os comandos dispon�veis para grupos.
	 */

	public void setGroupCommands(String groupCommands)
	{
		this.groupCommands = groupCommands;
	}

	/**
	 * @return aquisi��o do nome da tabela com as permiss�es dispon�veis para grupos.
	 */

	public String getGroupPermissions()
	{
		return groupPermissions;
	}

	/**
	 * @param groupPermissions nome da tabela com as permiss�es dispon�veis para grupos.
	 */

	public void setGroupPermissions(String groupPermissions)
	{
		this.groupPermissions = groupPermissions;
	}

	/**
	 * @return aquisi��o do nome da tabela contendo as configura��es dos acessos vip.
	 */

	public String getVip()
	{
		return vip;
	}

	/**
	 * @param vip nome da tabela contendo as configura��es dos acessos vip.
	 */

	public void setVip(String vip)
	{
		this.vip = vip;
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
	 * @return aquisi��o do nome da tabela contendo a experi�ncias obtidas pelos personagens.
	 */

	public String getCharExperiences()
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

package org.diverproject.jragnarok.server;

/**
 * <h1>Tabelas</h1>
 *
 * <p>Classe que possui o nome de todas as tabelas do banco de dados.
 * Assim é possível alterar o nome de uma tabela de forma mais simples no sistema.
 * Basta alterar através de um setter o nome da tabela e será aplicado ao sistema.
 * Para que essa classe funcione de fato as queries devem usar essa classe.</p>
 *
 * @author Andrew
 */

public class Tables
{
	/**
	 * Única instância disponível de Tabelas.
	 */
	private static final Tables INSTANCE = new Tables();

	/**
	 * Tabelas utiliza o Padrão de Projetos Singleton.
	 * Por esse motivo é necessário a existência desse método para obtê-lo.
	 * @return aquisição da única instância de Tabelas.
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
	 * Tabela contendo os detalhes de código pin dos jogadores.
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
	 * Tabela contendo todas as permissões habilitadas dos grupos.
	 */
	private String groupPermissionsList;

	/**
	 * Tabela contendo todos os comandos para jogadores.
	 */
	private String groupCommands;

	/**
	 * Tabela contendo todas as permissões para jogadores.
	 */
	private String groupPermissions;

	/**
	 * Tabela contendo as configurações dos acessos vip.
	 */
	private String vip;

	/**
	 * Tabela contendo a lista de endereços IP banidos.
	 */
	private String ipBan;

	/**
	 * Tabela contendo todos os dados básicos dos personagens.
	 */
	private String characters;

	/**
	 * Tabela contendo todas as experiências obtidas pelos personagens.
	 */
	private String charExperiences;

	/**
	 * Tabela contendo dados de família dos personagens.
	 */
	private String charFamily;

	/**
	 * Tabela contendo dados das localizações salvas dos personagens.
	 */
	private String charLocations;

	/**
	 * Tabela contendo dados de classificação de assistentes dos personagens.
	 */
	private String charMercenaryRank;

	/**
	 * Construtor privado para aderir ao Padrão de Projetos: Singleton.
	 * Define o nome padrão de todas as tabelas do banco de dados.
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
	 * @return aquisição do nome da tabela que registra o acesso de contas.
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
	 * @return aquisição do nome da tabela contendo informações das contas dos jogadores.
	 */

	public String getAccounts()
	{
		return accounts;
	}

	/**
	 * @param account nome da tabela contendo informações das contas dos jogadores.
	 */

	public void setAccounts(String account)
	{
		this.accounts = account;
	}

	/**
	 * @return aquisição do nome da tabela com detalhes do código pin dos jogadores.
	 */

	public String getPincodes()
	{
		return pincodes;
	}

	/**
	 * @param pincode nome da tabela com detalhes do código pin dos jogadores.
	 */

	public void setPincodes(String pincode)
	{
		this.pincodes = pincode;
	}

	/**
	 * @return aquisição do nome da tabela contendo o agrupamento dos jogadores.
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
	 * @return aquisição do nome da tabela com propriedades dos agrupamentos de jogadores.
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
	 * @return aquisição do nome da tabela contendo todos os comandos habilitados dos grupos.
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
	 * @return aquisição do nome da tabela contendo todas as permissões habilitados dos grupos.
	 */

	public String getGroupPermissionsList()
	{
		return groupPermissionsList;
	}

	/**
	 * @param groupPermissions nome da tabela contendo todas as permissões habilitados dos grupos.
	 */

	public void setGroupPermissionsList(String groupPermissions)
	{
		this.groupPermissionsList = groupPermissions;
	}

	/**
	 * @return aquisição do nome da tabela com os comandos disponíveis para grupos.
	 */

	public String getGroupCommands()
	{
		return groupCommands;
	}

	/**
	 * @param groupCommands nome da tabela com os comandos disponíveis para grupos.
	 */

	public void setGroupCommands(String groupCommands)
	{
		this.groupCommands = groupCommands;
	}

	/**
	 * @return aquisição do nome da tabela com as permissões disponíveis para grupos.
	 */

	public String getGroupPermissions()
	{
		return groupPermissions;
	}

	/**
	 * @param groupPermissions nome da tabela com as permissões disponíveis para grupos.
	 */

	public void setGroupPermissions(String groupPermissions)
	{
		this.groupPermissions = groupPermissions;
	}

	/**
	 * @return aquisição do nome da tabela contendo as configurações dos acessos vip.
	 */

	public String getVip()
	{
		return vip;
	}

	/**
	 * @param vip nome da tabela contendo as configurações dos acessos vip.
	 */

	public void setVip(String vip)
	{
		this.vip = vip;
	}

	/**
	 * @return aquisição do nome da tabela contendo a lista de endereços IP banidos.
	 */

	public String getIpBan()
	{
		return this.ipBan;
	}

	/**
	 * @param ipBan nome da tabela contendo a lista de endereços IP banidos.
	 */

	public void setIpBan(String ipBan)
	{
		this.ipBan = ipBan;
	}

	/**
	 * @return aquisição do nome da tabela contendo os dados básicos dos personagens.
	 */

	public String getCharacters()
	{
		return characters;
	}

	/**
	 * @param ipBan nome da tabela contendo os dados básicos dos personagens.
	 */

	public void setCharacters(String characters)
	{
		this.characters = characters;
	}

	/**
	 * @return aquisição do nome da tabela contendo a experiências obtidas pelos personagens.
	 */

	public String getCharExperiences()
	{
		return charExperiences;
	}

	/**
	 * @param experiences nome da tabela contendo a experiências obtidas pelos personagens.
	 */

	public void setCharExperiences(String experiences)
	{
		this.charExperiences = experiences;
	}

	/**
	 * @return aquisição do nome da tabela contendo dados de família dos personagens.
	 */

	public String getCharFamily()
	{
		return charFamily;
	}

	/**
	 * @param charFamily nome da tabela contendo dados de família dos personagens.
	 */

	public void setCharFamily(String charFamily)
	{
		this.charFamily = charFamily;
	}

	/**
	 * @return aquisição do nome da tabela contendo dados das localizações salvas personagens.
	 */

	public String getCharLocations()
	{
		return charLocations;
	}

	/**
	 * @param charLocations nome da tabela contendo dados das localizações salvas personagens.
	 */

	public void setCharLocations(String charLocations)
	{
		this.charLocations = charLocations;
	}

	/**
	 * @return aquisição do nome da tabela contendo dados de classificação de assistentes dos personagens.
	 */

	public String getCharMercenaryRank()
	{
		return charMercenaryRank;
	}

	/**
	 * @param charMercenaryRank nome da tabela contendo dados de classificação de assistentes dos personagens.
	 */

	public void setCharMercenaryRank(String charMercenaryRank)
	{
		this.charMercenaryRank = charMercenaryRank;
	}
}

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
	 * Tabela contendo as contas dos jogadores.
	 */
	private String login;

	/**
	 * Tabela contendo os registros do acesso de contas.
	 */
	private String loginLog;

	/**
	 * Construtor privado para aderir ao Padrão de Projetos: Singleton.
	 * Define o nome padrão de todas as tabelas do banco de dados.
	 */

	private Tables()
	{
		login = "login";
		loginLog = "login_log";
	}

	/**
	 * @return aquisição do nome da tabela que possui as contas dos jogadores.
	 */

	public String getLogin()
	{
		return login;
	}

	/**
	 * @param login nome da tabela que possui as contas dos jogadores.
	 */

	public void setLogin(String login)
	{
		this.login = login;
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
}

package org.diverproject.jragnarok.server.login.controllers;

import static org.diverproject.log.LogSystem.logExeception;

import java.sql.Connection;

import org.diverproject.jragnaork.RagnarokException;
import org.diverproject.jragnarok.server.login.entities.Account;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.StringSimpleMap;

/**
 * <h1>Controle de Contas</h1>
 *
 * <p>O controle de contas permite buscar constas que estejam em cache ou no banco de dados.
 * Considera primeiramente os valores em cache para depois buscar pelo banco de dados.
 * Desta forma é possível agilizar o processo de busca por informações no sistema.</p>
 *
 * @see StringSimpleMap
 * @see AccountDAO
 *
 * @author Andrew
 */

public class AccountControl
{
	/**
	 * Mapeamento das contas já carregadas.
	 */
	private Map<String, Account> cache;

	/**
	 * DAO para comunicação com o banco de dados.
	 */
	private AccountDAO dao;

	/**
	 * Cria um novo controle para gerenciamentos de contas no sistema.
	 * @param connection referência da conexão a ser considerada.
	 */

	public AccountControl(Connection connection)
	{
		dao = new AccountDAO(connection);
		cache = new StringSimpleMap<>();
	}

	/**
	 * Permite selecionar os dados de uma determinada conta por nome de usuário.
	 * @param username nome de usuário da conta do qual deseja as informações.
	 * @return aquisição da conta respectiva ao nome de usuário acima.
	 */

	public Account get(String username)
	{
		Account account = cache.get(username);

		if (account == null)
		{
			try {
				account = dao.select(username);
			} catch (RagnarokException e) {
				logExeception(e);
			}

			if (account != null)
				cache.add(username, account);
		}

		return account;
	}

	/**
	 * Atualiza algumas das informações de uma determinada conta no banco de dados.
	 * As informações atualizadas são como: último acesso, endereço de ip, estado e outros.
	 * @param account referência da conta do qual deseja atualizar os dados.
	 * @return true se conseguir atualizar ou false caso contrário.
	 */

	public boolean save(Account account)
	{
		try {
			return dao.update(account);
		} catch (RagnarokException e) {
			logExeception(e);
		}

		return false;
	}
}

package org.diverproject.jragnarok.server.login.control;

import org.diverproject.jragnarok.server.login.structures.AuthNode;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Controle de Jogadores Online</h1>
 *
 * <p>Controle que permite ao servidor de acesso saber quais os jogadores que estão online.
 * Sempre que um novo jogador entrar no servidor será repassado a esse controle.
 * Deve garantir que jogadores possam ser adicionados, removidos e verificados.</p>
 *
 * @see IntegerLittleMap
 * @see AuthNode
 *
 * @author Andrew
 */

public class AuthControl
{
	/**
	 * Mapeamento dos jogadores autenticados.
	 */
	private final Map<Integer, AuthNode> cache;

	/**
	 * Cria um novo controle para gerenciar jogadores autenticados no sistema.
	 */

	public AuthControl()
	{
		cache = new IntegerLittleMap<>();
	}

	/**
	 * Permite selecionar informações de uma determinada conta autenticada.
	 * @param id código de identificação da conta desejada.
	 * @return aquisição do objeto contendo informações da autenticação.
	 */

	public AuthNode get(int id)
	{
		return cache.get(id);
	}

	/**
	 * Adiciona uma nova autenticação de conta efetuada no sistema.
	 * @param node nó contendo as informações da autenticação.
	 * @return true se adicionar ou false se já existir.
	 */

	public boolean add(AuthNode node)
	{
		return cache.add(node.getAccountID(), node);
	}

	/**
	 * Remove uma determinada autenticação pelo seu nó gerado.
	 * @param accountID código de identificação da conta.
	 * @return true se remover ou false se não encontrar.
	 */

	public boolean remove(int accountID)
	{
		return cache.removeKey(accountID);
	}

	/**
	 * Remove uma determinada autenticação pelo seu nó gerado.
	 * @param node nó contendo as informações da autenticação.
	 * @return true se remover ou false se não encontrar.
	 */

	public boolean remove(AuthNode node)
	{
		return cache.removeKey(node.getAccountID());
	}

	/**
	 * Remove a autenticação de todos os jogadores já autenticados.
	 */

	public void clear()
	{
		cache.clear();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("auths", cache.size());

		return description.toString();
	}
}

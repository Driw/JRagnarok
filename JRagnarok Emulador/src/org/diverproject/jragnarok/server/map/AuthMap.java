package org.diverproject.jragnarok.server.map;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * <h1>Controle de Jogadores Online</h1>
 *
 * <p>Controle que permite ao servidor de mapa saber quais os jogadores que est�o online.
 * Sempre que um novo jogador entrar no servidor ser� repassado a esse controle.
 * Deve garantir que jogadores possam ser adicionados, removidos e verificados.</p>
 *
 * @see IntegerLittleMap
 * @see AuthNode
 *
 * @author Andrew
 */

public class AuthMap
{
	/**
	 * Mapeamento dos jogadores autenticados.
	 */
	private final Map<Integer, AuthNode> cache;

	/**
	 * Cria um novo controle para gerenciar jogadores autenticados no sistema.
	 */

	public AuthMap()
	{
		cache = new IntegerLittleMap<>();
	}

	/**
	 * Permite selecionar informa��es de uma determinada conta autenticada.
	 * @param id c�digo de identifica��o da conta desejada.
	 * @return aquisi��o do objeto contendo informa��es da autentica��o.
	 */

	public AuthNode get(int id)
	{
		return cache.get(id);
	}

	/**
	 * Adiciona uma nova autentica��o de conta efetuada no sistema.
	 * @param node n� contendo as informa��es da autentica��o.
	 * @return true se adicionar ou false se j� existir.
	 */

	public boolean add(AuthNode node)
	{
		return cache.add(node.getAccountID(), node);
	}

	/**
	 * Remove uma determinada autentica��o pelo seu n� gerado.
	 * @param accountID c�digo de identifica��o da conta.
	 * @return true se remover ou false se n�o encontrar.
	 */

	public boolean remove(int accountID)
	{
		return cache.removeKey(accountID);
	}

	/**
	 * Remove uma determinada autentica��o pelo seu n� gerado.
	 * @param node n� contendo as informa��es da autentica��o.
	 * @return true se remover ou false se n�o encontrar.
	 */

	public boolean remove(AuthNode node)
	{
		return cache.removeKey(node.getAccountID());
	}

	/**
	 * Remove a autentica��o de todos os jogadores j� autenticados.
	 */

	public void clear()
	{
		cache.clear();
	}

	/**
	 * Remove todas as autentica��es que n�o s�o mais v�lidas para estarem autenticadas.
	 * A invalidez pode ser por c�digo de identifica��o da conta/personagem ou conex�o.
	 */

	public void cleanup()
	{
		for (AuthNode node : cache)
			if (!node.getFileDescriptor().isConnected() || node.getAccountID() == 0 ||
				node.getCharacter() == null || node.getCharacter().getID() == 0)
				remove(node);
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("auths", cache.size());

		return description.toString();
	}
}

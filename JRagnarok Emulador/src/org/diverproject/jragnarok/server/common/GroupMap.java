package org.diverproject.jragnarok.server.common;

import java.util.Iterator;

import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * Mapeamento de Grupos
 *
 * Estrutura de dados utilizada para armazenar objetos que cont�m as informa��es dos grupos de contas dos jogadores.
 * A estrutura utilizada � tipo mapa que considera sua chave como um n�mero inteiro (c�digo de identifica��o do grupo).
 * Implementa apenas os m�todos necess�rios para que seja poss�vel gerenciar os objetos aqui contidos.
 *
 * @see Group
 * @see Iterable
 * @see IntegerLittleMap
 *
 * @author Andrew
 */

public class GroupMap implements Iterable<Group>
{
	/**
	 * Estrutura de dados que ir� armazenar os grupos dos jogadores.
	 */
	private Map<Integer, Group> groups;

	/**
	 * Cria uma nova inst�ncia de um mapeamento para grupo de jogadores inicializado a sua estrutura de dados.
	 */

	public GroupMap()
	{
		groups = new IntegerLittleMap<>();
	}

	/**
	 * @param gid c�digo de identifica��o do grupo do qual deseja obter na estrutura.
	 * @return aquisi��o do grupo de jogadores correspondente ao c�digo especificado
	 * ou null caso o c�digo especificado seja inv�lido ou n�o utilizado.
	 */

	public Group get(int gid)
	{
		return groups.get(gid);
	}

	/**
	 * @param group refer�ncia do objeto contendo as informa��es do grupo de jogadores
	 * @return true se conseguir adicionar ou false se o c�digo j� estiver sendo utilizado.
	 */

	public boolean add(Group group)
	{
		if (group != null && group.getID() > 0)
			return groups.add(group.getID(), group);

		return false;
	}

	/**
	 * @param gid c�digo de identifica��o do grupo do qual deseja remover da estrutura.
	 * @return true se conseguir encontrar e remover ou false se n�o encontrar.
	 */

	public boolean remove(int gid)
	{
		return groups.removeKey(gid);
	}

	/**
	 * Remove todos os grupos de jogadores da estrutura de mapeamento.
	 */

	public void clear()
	{
		groups.clear();
	}

	/**
	 * @return aquisi��o da quantidade de grupos que j� foram adicionados a esse mapeamento.
	 */

	public int size()
	{
		return groups.size();
	}

	@Override
	public Iterator<Group> iterator()
	{
		return groups.iterator();
	}

	@Override
	public String toString()
	{
		return groups.toString();
	}
}

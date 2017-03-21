package org.diverproject.jragnarok.server.common;

import java.util.Iterator;

import org.diverproject.jragnarok.server.common.entities.Group;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;

/**
 * Mapeamento de Grupos
 *
 * Estrutura de dados utilizada para armazenar objetos que contém as informações dos grupos de contas dos jogadores.
 * A estrutura utilizada é tipo mapa que considera sua chave como um número inteiro (código de identificação do grupo).
 * Implementa apenas os métodos necessários para que seja possível gerenciar os objetos aqui contidos.
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
	 * Estrutura de dados que irá armazenar os grupos dos jogadores.
	 */
	private Map<Integer, Group> groups;

	/**
	 * Cria uma nova instância de um mapeamento para grupo de jogadores inicializado a sua estrutura de dados.
	 */

	public GroupMap()
	{
		groups = new IntegerLittleMap<>();
	}

	/**
	 * @param gid código de identificação do grupo do qual deseja obter na estrutura.
	 * @return aquisição do grupo de jogadores correspondente ao código especificado
	 * ou null caso o código especificado seja inválido ou não utilizado.
	 */

	public Group get(int gid)
	{
		return groups.get(gid);
	}

	/**
	 * @param group referência do objeto contendo as informações do grupo de jogadores
	 * @return true se conseguir adicionar ou false se o código já estiver sendo utilizado.
	 */

	public boolean add(Group group)
	{
		if (group != null && group.getID() > 0)
			return groups.add(group.getID(), group);

		return false;
	}

	/**
	 * @param gid código de identificação do grupo do qual deseja remover da estrutura.
	 * @return true se conseguir encontrar e remover ou false se não encontrar.
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
	 * @return aquisição da quantidade de grupos que já foram adicionados a esse mapeamento.
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

package org.diverproject.jragnarok.server.login;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Node;

/**
 * <h1>Nó de Hash para Cliente</h1>
 *
 * <p>Através de um hash é possível autenticar a utilização de um determinado cliente executável.
 * Cada versão de um cliente executável possui um hash que irá identificá-lo e é recebido pelo servidor.
 * Assim o servidor de acesso pode autorizar ou não o acesso desse cliente através do seu hash.</p>
 *
 * <p>Sua utilidade entra de forma que seja possível restringir acesso de clientes que não possuem
 * um hash especificado por configuração, evitando assim que o cliente possa ter sofrido modificações.
 * Limita ainda o acesso de jogadores que utilizem um cliente executável diferente do fornecido.</p>
 *
 * @see Node
 * @see ClientHash
 *
 * @author Andrew
 */

public class ClientHashNode extends Node<ClientHash>
{
	/**
	 * Nível de acesso do grupo de jogadores que poderá ignorar esse hash.
	 */
	private int groupLevel;

	/**
	 * Cria uma nova instância de um nó de hash para cliente inicializando o nó.
	 * Uma vez definido um hash para este nó ele não poderá ser modificado.
	 * @param value referência do hash para cliente que será usado neste nó.
	 */

	public ClientHashNode(ClientHash value)
	{
		super(value);
	}

	/**
	 * @return aquisição do nível de acesso mínimo para ignorar a verificação desse hash.
	 */

	public int getGroupLevel()
	{
		return groupLevel;
	}

	/**
	 * @param groupID nível de acesso mínimo para ignorar a verificação desse hash.
	 */

	public void setGroupLevel(int groupID)
	{
		this.groupLevel = groupID;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("groupLevel", groupLevel);
		description.append("hash", get().getHashString());
		description.append("prev", getPrev() == null ? null : getPrev().get().getHashString());
		description.append("next", getNext() == null ? null : getNext().get().getHashString());

		return description.toString();
	}

}

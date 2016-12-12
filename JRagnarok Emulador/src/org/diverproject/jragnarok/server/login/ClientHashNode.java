package org.diverproject.jragnarok.server.login;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.collection.Node;

/**
 * <h1>N� de Hash para Cliente</h1>
 *
 * <p>Atrav�s de um hash � poss�vel autenticar a utiliza��o de um determinado cliente execut�vel.
 * Cada vers�o de um cliente execut�vel possui um hash que ir� identific�-lo e � recebido pelo servidor.
 * Assim o servidor de acesso pode autorizar ou n�o o acesso desse cliente atrav�s do seu hash.</p>
 *
 * <p>Sua utilidade entra de forma que seja poss�vel restringir acesso de clientes que n�o possuem
 * um hash especificado por configura��o, evitando assim que o cliente possa ter sofrido modifica��es.
 * Limita ainda o acesso de jogadores que utilizem um cliente execut�vel diferente do fornecido.</p>
 *
 * @see Node
 * @see ClientHash
 *
 * @author Andrew
 */

public class ClientHashNode extends Node<ClientHash>
{
	/**
	 * N�vel de acesso do grupo de jogadores que poder� ignorar esse hash.
	 */
	private int groupLevel;

	/**
	 * Cria uma nova inst�ncia de um n� de hash para cliente inicializando o n�.
	 * Uma vez definido um hash para este n� ele n�o poder� ser modificado.
	 * @param value refer�ncia do hash para cliente que ser� usado neste n�.
	 */

	public ClientHashNode(ClientHash value)
	{
		super(value);
	}

	/**
	 * @return aquisi��o do n�vel de acesso m�nimo para ignorar a verifica��o desse hash.
	 */

	public int getGroupLevel()
	{
		return groupLevel;
	}

	/**
	 * @param groupID n�vel de acesso m�nimo para ignorar a verifica��o desse hash.
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

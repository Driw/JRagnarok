package org.diverproject.jragnarok.server.character;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

/**
 * <h1>Dados de Personagem</h1>
 *
 * <p>Esses dados s�o b�sicos e armazenados nas sess�es referentes as conex�es com o servidor de personagem.
 * Guarda informa��es para agilizar alguns processos evitando solicita��es com o banco de dados.
 * As informa��es s�o de: identifica��o do personagem, quantas vezes foi movimentado e hor�rio de fim do banimento.</p>
 *
 * @see Time
 *
 * @author Andrew
 */

public class CharData
{
	/**
	 * C�digo de identifica��o do personagem.
	 */
	private int id;

	/**
	 * Quantidade de vezes que o personagem foi movido de slot.
	 */
	private int charMove;

	/**
	 * Hor�rio de fim do banimento (0: n�o est� banido).
	 */
	private Time unban;

	/**
	 * Cria uma nova inst�ncia para armazenar dados de um personagem.
	 * Inicializa o hor�rio de fim do banimento do personagem como: n�o banido.
	 */

	public CharData()
	{
		unban = new Time();
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do personagem.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id c�digo de identifica��o do personagem.
	 */

	public void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisi��o da quantidade de vezes que o personagem foi movido de slot.
	 */

	public int getCharMove()
	{
		return charMove;
	}

	/**
	 * @param charMove quantidade de vezes que o personagem foi movido de slot.
	 */

	public void setCharMove(int charMove)
	{
		this.charMove = charMove;
	}

	/**
	 * @return aquisi��o do hor�rio de fim do banimento do personagem.
	 */

	public Time getUnban()
	{
		return unban;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("charMove", charMove);
		description.append("uban", unban);

		return description.toString();
	}
}

package org.diverproject.jragnarok.server.character;

import org.diverproject.util.ObjectDescription;
import org.diverproject.util.Time;

/**
 * <h1>Dados de Personagem</h1>
 *
 * <p>Esses dados são básicos e armazenados nas sessões referentes as conexões com o servidor de personagem.
 * Guarda informações para agilizar alguns processos evitando solicitações com o banco de dados.
 * As informações são de: identificação do personagem, quantas vezes foi movimentado e horário de fim do banimento.</p>
 *
 * @see Time
 *
 * @author Andrew
 */

public class CharData
{
	/**
	 * Código de identificação do personagem.
	 */
	private int id;

	/**
	 * Quantidade de vezes que o personagem foi movido de slot.
	 */
	private int charMove;

	/**
	 * Horário de fim do banimento (0: não está banido).
	 */
	private Time unban;

	/**
	 * Cria uma nova instância para armazenar dados de um personagem.
	 * Inicializa o horário de fim do banimento do personagem como: não banido.
	 */

	public CharData()
	{
		unban = new Time();
	}

	/**
	 * @return aquisição do código de identificação do personagem.
	 */

	public int getID()
	{
		return id;
	}

	/**
	 * @param id código de identificação do personagem.
	 */

	public void setID(int id)
	{
		this.id = id;
	}

	/**
	 * @return aquisição da quantidade de vezes que o personagem foi movido de slot.
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
	 * @return aquisição do horário de fim do banimento do personagem.
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

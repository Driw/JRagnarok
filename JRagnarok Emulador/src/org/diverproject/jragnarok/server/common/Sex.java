package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.RagnarokRuntimeException;

/**
 * <h1>Enumera��o</h1>
 *
 * <p>Enumera��o para definir o sexo dispon�vel para contas e/ou personagens.
 * H� dispon�vel apenas tr�s op��es: masculino, feminino e servidor.
 * No caso do servidor o sexo indica que n�o � uma conta/personagem v�lido.</p>
 *
 * @author Andrew
 */

public enum Sex
{
	/**
	 * Sexo feminino.
	 */
	FEMALE('F'),

	/**
	 * Sexo masculino.
	 */
	MALE('M'),

	/**
	 * Sexo para servidor (inv�lido).
	 */
	SERVER('S');

	/**
	 * Constante que determina o valor do sexo (banco de dados ou pacote).
	 */
	public final char c;

	/**
	 * Cria uma nova inst�ncia de uma enumera��o para definir o sexo de um personagem/conta.
	 * @param c caracter que ir� representar o sexo e este n�o deve se repetir.
	 */

	private Sex(char c)
	{
		this.c = c;
	}

	/**
	 * @return aquisi��o do c�digo (byte) da representa��o do sexo (caracter).
	 */

	public byte code()
	{
		return b(ordinal());
	}

	/**
	 * Transforma um determinado caracter especificado em seus respectivo sexo no enumerador.
	 * @param c caracter do qual deseja analisar e obter a enumera��o referente ao mesmo.
	 * @return aquisi��o do enumerador referente ao sexo representado pelo caracter passado.
	 */

	public static final Sex parse(char c)
	{
		switch (c)
		{
			case 'F': return FEMALE;
			case 'M': return MALE;
			case 'S': return SERVER;
		}

		throw new RagnarokRuntimeException("Sex#%s n�o encontrado", c);
	}

	/**
	 * Transforma um determinado c�digo especificado em seus respectivos sexo no enumerador.
	 * @param code c�digo do qual deseja analisar e obter a enumera��o referente ao mesmo.
	 * @return aquisi��o do enumerador referente ao sexo representado pelo c�digo passado.
	 */

	public static final Sex parse(byte code)
	{
		switch (code)
		{
			case 0: return FEMALE;
			case 1: return MALE;
			case 2: return SERVER;
		}

		throw new RagnarokRuntimeException("Sex#%d n�o encontrado", code);
	}
}

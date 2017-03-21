package org.diverproject.jragnarok.server.common;

import static org.diverproject.util.Util.b;

import org.diverproject.jragnarok.RagnarokRuntimeException;

/**
 * <h1>Enumeração</h1>
 *
 * <p>Enumeração para definir o sexo disponível para contas e/ou personagens.
 * Há disponível apenas três opções: masculino, feminino e servidor.
 * No caso do servidor o sexo indica que não é uma conta/personagem válido.</p>
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
	 * Sexo para servidor (inválido).
	 */
	SERVER('S');

	/**
	 * Constante que determina o valor do sexo (banco de dados ou pacote).
	 */
	public final char c;

	/**
	 * Cria uma nova instância de uma enumeração para definir o sexo de um personagem/conta.
	 * @param c caracter que irá representar o sexo e este não deve se repetir.
	 */

	private Sex(char c)
	{
		this.c = c;
	}

	/**
	 * @return aquisição do código (byte) da representação do sexo (caracter).
	 */

	public byte code()
	{
		return b(ordinal());
	}

	/**
	 * Transforma um determinado caracter especificado em seus respectivo sexo no enumerador.
	 * @param c caracter do qual deseja analisar e obter a enumeração referente ao mesmo.
	 * @return aquisição do enumerador referente ao sexo representado pelo caracter passado.
	 */

	public static final Sex parse(char c)
	{
		switch (c)
		{
			case 'F': return FEMALE;
			case 'M': return MALE;
			case 'S': return SERVER;
		}

		throw new RagnarokRuntimeException("Sex#%s não encontrado", c);
	}

	/**
	 * Transforma um determinado código especificado em seus respectivos sexo no enumerador.
	 * @param code código do qual deseja analisar e obter a enumeração referente ao mesmo.
	 * @return aquisição do enumerador referente ao sexo representado pelo código passado.
	 */

	public static final Sex parse(byte code)
	{
		switch (code)
		{
			case 0: return FEMALE;
			case 1: return MALE;
			case 2: return SERVER;
		}

		throw new RagnarokRuntimeException("Sex#%d não encontrado", code);
	}
}

package org.diverproject.jragnarok.util;

import static org.diverproject.jragnarok.JRagnarokConstants.MAP_CHUNK_SIZE;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_SIZE;
import static org.diverproject.util.lang.IntUtil.limit;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Dimens�o de Mapa</h1>
 *
 * <p>Objeto que permite especificar as dimens�es (largura x comprimento) do tamanho de um mapa.
 * Atrav�s dele � poss�vel especificar regras do sistema que impossibilitam valores fora dos limites.</p>
 *
 * <p>Os valores limites s�o definidos por <code>MAP_CHUNK_SIZE</code> e <code>MAX_MAP_SIZE</code>.
 * O primeiro determina que um mapa n�o pode ser menor que uma chunk, j� que � a divis�o de um mapa.
 * J� o segundo delimita um tamanho m�ximo aos mapas para evitar mapas gigantescos e fora do comum.</p>
 *
 * @author Andrew
 */

public class MapDimension
{
	/**
	 * Tamanho da largura do mapa em c�lulas.
	 */
	private int width;

	/**
	 * Tamanho do comprimento do mapa em c�lulas.
	 */
	private int length;

	/**
	 * Cria uma nova inst�ncia para um objeto de dimens�o de mapas iniciando os valores m�nimos permitidos.
	 */

	public MapDimension()
	{
		this(MAP_CHUNK_SIZE, MAP_CHUNK_SIZE);
	}

	/**
	 * Cria uma nova inst�ncia para um objeto de dimens�o de mapas iniciando os valores abaixo:
	 * @param width quantidade de c�lulas que determinam a largura do mapa.
	 * @param length quantidade de c�lulas que determinam o comprimento do mapa.
	 */

	public MapDimension(int width, int length)
	{
		setWidth(width);
		setLength(length);
	}

	/**
	 * @return aquisi��o da quantidade de c�lulas referente a largura do mapa.
	 */

	public int getWidth()
	{
		return width;
	}

	/**
	 * O valor passado deve estar entre <code>MAP_CHUNK_SIZE</code> e <code>MAX_MAP_SIZE</code>.
	 * @param width quantidade de c�lulas referente a largura do mapa.
	 */

	public void setWidth(int width)
	{
		this.width = limit(width, MAP_CHUNK_SIZE, MAX_MAP_SIZE);
	}

	/**
	 * @return aquisi��o da quantidade de c�lulas referente ao comprimento do mapa.
	 */

	public int getLength()
	{
		return length;
	}

	/**
	 * O valor passado deve estar entre <code>MAP_CHUNK_SIZE</code> e <code>MAX_MAP_SIZE</code>.
	 * @param length quantidade de c�lulas referente ao comprimento do mapa.
	 */

	public void setLength(int length)
	{
		this.length = limit(length, MAP_CHUNK_SIZE, MAX_MAP_SIZE);
	}

	/**
	 * Os valores passados devem estar entre <code>MAP_CHUNK_SIZE</code> e <code>MAX_MAP_SIZE</code>.
	 * @param width quantidade de c�lulas referente a largura do mapa.
	 * @param length quantidade de c�lulas referente ao comprimento do mapa.
	 */

	public void setSize(int width, int length)
	{
		setWidth(width);
		setLength(length);
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof MapDimension)
		{
			MapDimension dimension = (MapDimension) object;

			return this.width == dimension.width && this.length == dimension.length;
		}

		return false;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("width", width);
		description.append("length", length);

		return description.toString();
	}
}

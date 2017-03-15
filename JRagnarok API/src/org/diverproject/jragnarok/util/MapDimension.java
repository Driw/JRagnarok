package org.diverproject.jragnarok.util;

import static org.diverproject.jragnarok.JRagnarokConstants.MAP_CHUNK_SIZE;
import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_SIZE;
import static org.diverproject.util.lang.IntUtil.limit;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Dimensão de Mapa</h1>
 *
 * <p>Objeto que permite especificar as dimensões (largura x comprimento) do tamanho de um mapa.
 * Através dele é possível especificar regras do sistema que impossibilitam valores fora dos limites.</p>
 *
 * <p>Os valores limites são definidos por <code>MAP_CHUNK_SIZE</code> e <code>MAX_MAP_SIZE</code>.
 * O primeiro determina que um mapa não pode ser menor que uma chunk, já que é a divisão de um mapa.
 * Já o segundo delimita um tamanho máximo aos mapas para evitar mapas gigantescos e fora do comum.</p>
 *
 * @author Andrew
 */

public class MapDimension
{
	/**
	 * Tamanho da largura do mapa em células.
	 */
	private int width;

	/**
	 * Tamanho do comprimento do mapa em células.
	 */
	private int length;

	/**
	 * Cria uma nova instância para um objeto de dimensão de mapas iniciando os valores mínimos permitidos.
	 */

	public MapDimension()
	{
		this(MAP_CHUNK_SIZE, MAP_CHUNK_SIZE);
	}

	/**
	 * Cria uma nova instância para um objeto de dimensão de mapas iniciando os valores abaixo:
	 * @param width quantidade de células que determinam a largura do mapa.
	 * @param length quantidade de células que determinam o comprimento do mapa.
	 */

	public MapDimension(int width, int length)
	{
		setWidth(width);
		setLength(length);
	}

	/**
	 * @return aquisição da quantidade de células referente a largura do mapa.
	 */

	public int getWidth()
	{
		return width;
	}

	/**
	 * O valor passado deve estar entre <code>MAP_CHUNK_SIZE</code> e <code>MAX_MAP_SIZE</code>.
	 * @param width quantidade de células referente a largura do mapa.
	 */

	public void setWidth(int width)
	{
		this.width = limit(width, MAP_CHUNK_SIZE, MAX_MAP_SIZE);
	}

	/**
	 * @return aquisição da quantidade de células referente ao comprimento do mapa.
	 */

	public int getLength()
	{
		return length;
	}

	/**
	 * O valor passado deve estar entre <code>MAP_CHUNK_SIZE</code> e <code>MAX_MAP_SIZE</code>.
	 * @param length quantidade de células referente ao comprimento do mapa.
	 */

	public void setLength(int length)
	{
		this.length = limit(length, MAP_CHUNK_SIZE, MAX_MAP_SIZE);
	}

	/**
	 * Os valores passados devem estar entre <code>MAP_CHUNK_SIZE</code> e <code>MAX_MAP_SIZE</code>.
	 * @param width quantidade de células referente a largura do mapa.
	 * @param length quantidade de células referente ao comprimento do mapa.
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

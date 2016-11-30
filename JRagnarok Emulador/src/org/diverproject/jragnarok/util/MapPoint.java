package org.diverproject.jragnarok.util;

import static org.diverproject.jragnarok.JRagnarokUtil.format;

import org.diverproject.util.ObjectDescription;

/**
 * <h1>Ponto no Mapa</h1>
 *
 * <p>Identifica uma ponto (coordenadas) em um determinado mapa.</p>
 *
 * @see Point
 *
 * @author Andrew
 */

public class MapPoint extends Point
{
	/**
	 * Nome do mapa.
	 */
	private String map;

	/**
	 * @return aquisição do nome do mapa.
	 */

	public String getMap()
	{
		return map;
	}

	/**
	 * @param map nome do mapa.
	 */

	public void setMap(String map)
	{
		this.map = map;
	}

	/**
	 * Verifica se as coordenadas estão como nulo (x e y = zero).
	 * @return true se for nulo ou false caso contrário.
	 */

	public boolean isNull()
	{
		return getX() == 0 && getY() == 0;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("map", map);
		description.append("coord", format("%d,%d", getX(), getY()));

		return description.toString();
	}
}

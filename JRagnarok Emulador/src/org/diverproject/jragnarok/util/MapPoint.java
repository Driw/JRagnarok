package org.diverproject.jragnarok.util;

import static org.diverproject.util.Util.format;

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
	private short mapID;

	/**
	 * @return aquisi��o do c�digo de identifica��o do mapa.
	 */

	public short getMapID()
	{
		return mapID;
	}

	/**
	 * @param map c�digo de identifica��o do mapa.
	 */

	public void setMapID(short map)
	{
		this.mapID = map;
	}

	/**
	 * Verifica se as coordenadas est�o como nulo (x e y = zero).
	 * @return true se for nulo ou false caso contr�rio.
	 */

	public boolean isNull()
	{
		return getX() == 0 && getY() == 0;
	}

	@Override
	public MapPoint clone()
	{
		MapPoint point = new MapPoint();
		point.mapID = mapID;
		point.setX(getX());
		point.setY(getY());

		return point;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("map", mapID);
		description.append("coord", format("%d,%d", getX(), getY()));

		return description.toString();
	}
}

package org.diverproject.jragnarok.database.impl;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_NAME_LENGTH;
import static org.diverproject.jragnarok.JRagnarokConstants.MIN_MAP_NAME_LENGTH;
import static org.diverproject.util.Util.s;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.database.IndexableDatabaseItem;
import org.diverproject.util.ObjectDescription;

/**
 * <h1>Indexa��o do Mapa</h1>
 *
 * <p>Classe para especificar o c�digo de identifica��o �nico do mapa entre os servidores e o seu nome virtual.
 * Apenas essas duas informa��es s�o necess�rias para a indexa��o de um mapa entre os servidores.</p>
 *
 * @see IndexableDatabaseItem
 *
 * @author Andrew
 */

public class MapIndex implements IndexableDatabaseItem
{
	/**
	 * C�digo de identifica��o do mapa entre os servidores.
	 */
	private short mapID;

	/**
	 * Nome virtual �nico do mapa.
	 */
	private String mapName;

	@Override
	public int getID()
	{
		return mapID;
	}

	@Override
	public void setID(int id)
	{
		if (id >= 1)
			this.mapID = s(id);
	}

	/**
	 * @return aquisi��o do c�digo de identifica��o do mapa (formato original: short).
	 */

	public short getMapID()
	{
		return mapID;
	}

	/**
	 * @return aquisi��o do nome virtual �nico para f�cil identifica��o do mapa.
	 */

	public String getMapName()
	{
		return mapName;
	}

	/**
	 * @param mapName novo nome virtual �nico para f�cil identifica��o do mapa.
	 */

	public void setMapName(String mapName)
	{
		if (mapName != null && interval(mapName.length(), MIN_MAP_NAME_LENGTH, MAX_MAP_NAME_LENGTH))
			this.mapName = mapName;
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("mapID", mapID);
		description.append("mapName", mapName);

		return description.toString();
	}
}

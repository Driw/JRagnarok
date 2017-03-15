package org.diverproject.jragnarok.database;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_INDEX;
import static org.diverproject.jragnarok.JRagnarokConstants.MIN_MAP_NAME_LENGTH;

import org.diverproject.jragnarok.database.impl.MapData;

/**
 * <h1>Cache para Mapa</h1>
 *
 * <p>Estrutura de dados para armazenas as informa��es e propriedades b�sicas para funcionamento dos mapas.
 * Essas informa��es podem ser obtidos de um Map Cache contido em um arquivo TXT ou banco de dados SQL.
 * Implementa todos os m�todos para funcionar como uma base de dados no sistema (API e SDK).</p>
 *
 * @see IndexableDatabaseItem
 * @see MapData
 *
 * @author Andrew
 */

public class MapCache extends IndexableDatabase<MapData>
{
	/**
	 * Cria uma nova inst�ncia de uma estrutura de dados para um Map Cache.
	 * Define o tipo gen�rico para itera��es, nome e limite de registros.
	 */

	public MapCache()
	{
		super(MapData.class, "MapCacheDB", MAX_MAP_INDEX);
	}

	@Override
	public boolean insert(MapData item)
	{
		if (item == null || contains(item.getName()))
			return false;

		return super.insert(item);
	}

	/**
	 * Verifica existe um mapa em cache que esteja usando um nome virtual especifico.
	 * @param mapname nome virtual do mapa do qual deseja verificar se est� contido.
	 * @return true se existir em cache ou false caso contr�rio.
	 */

	public boolean contains(String mapname)
	{
		return select(mapname) != null;
	}

	/**
	 * Permite selecionar os dados de um mapa trav�s do seu nome virtual que � �nico.
	 * @param mapname nome virtual do mapa do qual deseja selecionar em cache.
	 * @return refer�ncia do objeto contendo os dados do mapa ou null se n�o existir o nome virutal.
	 */

	public MapData select(String mapname)
	{
		if (mapname == null || mapname.length() < MIN_MAP_NAME_LENGTH)
			return null;

		for (int i = 0; i < items.length; i++)
			if (items[i] != null && items[i].getName().equals(mapname))
				return items[i];

		return null;
	}
}

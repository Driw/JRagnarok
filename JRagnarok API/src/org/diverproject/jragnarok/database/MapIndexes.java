package org.diverproject.jragnarok.database;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_INDEX;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.database.impl.MapIndex;

/**
 * <h1>�ndice para Mapa</h1>
 *
 * <p>Estrutura de dados para armazenas o c�digo de identifica��o �nico dos mapas de todos os servidores..
 * Essas informa��es podem ser obtidos de lista contido em um arquivo TXT ou banco de dados SQL.
 * Implementa todos os m�todos para funcionar como uma base de dados no sistema (API e SDK).</p>
 *
 * <p>Essa indexa��o � necess�ria para que um mesmo mapa possua o mesmo �ndice em todos os servidores.
 * Isso ocorre devido ao fato de quem nem todos os servidores de mapa s�o obtidos a terem os mesmos mapas.
 * Assim sendo se for auto incremental a identifica��o poder� variar entre os servidores.</p>
 *
 * @see IndexableDatabaseItem
 * @see MapIndex
 *
 * @author Andrew
 */

public class MapIndexes extends IndexableDatabase<MapIndex>
{
	/**
	 * Cria uma nova inst�ncia de uma estrutura de dados para indexa��o dos mapas nos servidores.
	 */

	public MapIndexes()
	{
		super(MapIndex.class, "MapIndexDB", MAX_MAP_INDEX);
	}

	/**
	 * @param index n�mero do �ndice dentro da indexa��o dos mapas do qual deseja.
	 * @return aquisi��o da indexa��o do mapa conforme o �ndice especificado.
	 */

	public MapIndex get(int index)
	{
		if (interval(index, 1, items.length))
			return items[index - 1];

		return null;
	}

	/**
	 * @param mapname nome virtual do mapa do qual deseja saber a indexa��o.
	 * @return aquisi��o da indexa��o do mapa com o nome virtual ou zero se n�o existir.
	 */

	public int getMapID(String mapname)
	{
		for (MapIndex map : items)
			if (map != null && map.getMapName().equals(mapname))
				return map.getID();

		return 0;
	}

	/**
	 * @param index n�mero do �ndice dentro da indexa��o dos mapas do qual deseja.
	 * @return aquisi��o do nome virtual do mapa conforme o seu �ndice ou null se n�o existir.
	 */

	public String getMapName(int index)
	{
		if (interval(index, 1, length()))
			if (items[index - 1] != null)
				return items[index - 1].getMapName();

		return null;
	}

	/**
	 * Verifica se o nome de um determinado virtual se encontra indexado nesta estrutura de dados.
	 * @param mapname nome virtual do mapa do qual deseja verificar se est� aqui contido.
	 * @return true se estiver contido ou false caso contr�rio.
	 */

	public boolean contains(String mapname)
	{
		for (int i = 0, f = 0; i < length() && f < size(); i++)
			if (items[i] != null)
			{
				f++;

				if (items[i].getMapName().equals(mapname))
					return true;
			}

		return false;
	}
}

package org.diverproject.jragnarok.database;

import static org.diverproject.jragnarok.JRagnarokConstants.MAX_MAP_INDEX;
import static org.diverproject.util.lang.IntUtil.interval;

import org.diverproject.jragnarok.database.impl.MapIndex;

/**
 * <h1>Índice para Mapa</h1>
 *
 * <p>Estrutura de dados para armazenas o código de identificação único dos mapas de todos os servidores..
 * Essas informações podem ser obtidos de lista contido em um arquivo TXT ou banco de dados SQL.
 * Implementa todos os métodos para funcionar como uma base de dados no sistema (API e SDK).</p>
 *
 * <p>Essa indexação é necessária para que um mesmo mapa possua o mesmo índice em todos os servidores.
 * Isso ocorre devido ao fato de quem nem todos os servidores de mapa são obtidos a terem os mesmos mapas.
 * Assim sendo se for auto incremental a identificação poderá variar entre os servidores.</p>
 *
 * @see IndexableDatabaseItem
 * @see MapIndex
 *
 * @author Andrew
 */

public class MapIndexes extends IndexableDatabase<MapIndex>
{
	/**
	 * Cria uma nova instância de uma estrutura de dados para indexação dos mapas nos servidores.
	 */

	public MapIndexes()
	{
		super(MapIndex.class, "MapIndexDB", MAX_MAP_INDEX);
	}

	/**
	 * @param index número do índice dentro da indexação dos mapas do qual deseja.
	 * @return aquisição da indexação do mapa conforme o índice especificado.
	 */

	public MapIndex get(int index)
	{
		if (interval(index, 1, items.length))
			return items[index - 1];

		return null;
	}

	/**
	 * @param mapname nome virtual do mapa do qual deseja saber a indexação.
	 * @return aquisição da indexação do mapa com o nome virtual ou zero se não existir.
	 */

	public int getMapID(String mapname)
	{
		for (MapIndex map : items)
			if (map != null && map.getMapName().equals(mapname))
				return map.getID();

		return 0;
	}

	/**
	 * @param index número do índice dentro da indexação dos mapas do qual deseja.
	 * @return aquisição do nome virtual do mapa conforme o seu índice ou null se não existir.
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
	 * @param mapname nome virtual do mapa do qual deseja verificar se está aqui contido.
	 * @return true se estiver contido ou false caso contrário.
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

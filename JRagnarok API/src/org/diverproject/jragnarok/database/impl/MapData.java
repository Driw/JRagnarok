package org.diverproject.jragnarok.database.impl;

import static org.diverproject.util.Util.format;

import org.diverproject.jragnarok.database.IndexableDatabaseItem;
import org.diverproject.jragnarok.util.MapDimension;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SizeUtil;

/**
 * <h1>Dados de Mapa</h1>
 *
 * ´<p>Com esta classe será possível especificas as informações/propriedades diretas dos mapas de forma a conhecê-los.
 * As informações aqui são pertinentes as que existem no lado do cliente encontrados na pasta data ou arquivos GRF.</p>
 *
 * <p>Portanto é definido um número de identificação específico no Map Cache que é auto incremental e não muda.
 * Existe também um código de identificação do mapa que é para o jogo facilitar sua identificação ao invés de usar o nome.
 * Como dito, há também um nome do mapa que neste caso é um nome virtual, além da dimensão e objeto para mapear as células.</p>
 *
 * @see IndexableDatabaseItem
 * @see MapDimension
 * @see MapCell
 *
 * @author Andrew
 */

public class MapData implements IndexableDatabaseItem
{
	/**
	 * Número de identificação auto incremental no Map Cache.
	 */
	private int id;

	/**
	 * Código de identificação único por mapa em todos os servidores.
	 */
	private int mapIndex;

	/**
	 * Nome virtual para melhor identificação durante a criação do jogo.
	 */
	private String name;

	/**
	 * Objeto que especifica as dimensões do mapa.
	 */
	private MapDimension dimension;

	/**
	 * Mapeamento das propriedades das células do mapa.
	 */
	private MapCell mapCell;

	/**
	 * Cria uma nova instância para armazenar dados de um mapa especifico.
	 * Inicializa o objeto de dimensionamento do mapa e mapeamento das células.
	 */

	public MapData()
	{
		dimension = new MapDimension();
		mapCell = new MapCell();
	}

	@Override
	public int getID()
	{
		return id;
	}

	@Override
	public void setID(int id)
	{
		if (this.id == 0 && id > 0)
			this.id = id;
	}

	/**
	 * @return aquisição do código de identificação único por mapa.
	 */

	public int getMapIndex()
	{
		return mapIndex;
	}

	/**
	 * @param mapIndex código de identificação único por mapa.
	 */

	public void setMapIndex(int mapIndex)
	{
		this.mapIndex = mapIndex;
	}

	/**
	 * @return aquisição do nome virtual do mapa.
	 */

	public String getName()
	{
		return name;
	}

	/**
	 * @param name nome virtual do mapa.
	 */

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return aquisição das dimensões do mapa.
	 */

	public MapDimension getDimension()
	{
		return dimension;
	}

	/**
	 * @return aquisição do mapeamento das propriedades das células do mapa.
	 */

	public MapCell getMapCell()
	{
		return mapCell;
	}

	/**
	 * Atualiza o mapeamento das células com o dimensionamento atual do mapa.
	 * Em seguida inicializa o vetor que armazena as propriedades do mapa.
	 */

	public void initMapCell()
	{
		mapCell.setDimension(getDimension());
		mapCell.initData();
	}

	/**
	 * @return aquisição da quantidade de bytes que é gasta nas propriedades do mapa (células).
	 */

	public int getBufferSize()
	{
		return mapCell.getBufferSize();
	}

	@Override
	public String toString()
	{
		ObjectDescription description = new ObjectDescription(getClass());

		description.append("id", id);
		description.append("mapIndex", mapIndex);
		description.append("name", name);

		if (dimension != null)
			description.append("dimension", format("%dx%d", dimension.getWidth(), dimension.getLength()));

		description.append("buffer", SizeUtil.toString(getBufferSize()));

		return description.toString();
	}
}

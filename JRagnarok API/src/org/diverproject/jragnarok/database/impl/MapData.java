package org.diverproject.jragnarok.database.impl;

import static org.diverproject.util.Util.format;

import org.diverproject.jragnarok.database.IndexableDatabaseItem;
import org.diverproject.jragnarok.util.MapDimension;
import org.diverproject.util.ObjectDescription;
import org.diverproject.util.SizeUtil;

/**
 * <h1>Dados de Mapa</h1>
 *
 * �<p>Com esta classe ser� poss�vel especificas as informa��es/propriedades diretas dos mapas de forma a conhec�-los.
 * As informa��es aqui s�o pertinentes as que existem no lado do cliente encontrados na pasta data ou arquivos GRF.</p>
 *
 * <p>Portanto � definido um n�mero de identifica��o espec�fico no Map Cache que � auto incremental e n�o muda.
 * Existe tamb�m um c�digo de identifica��o do mapa que � para o jogo facilitar sua identifica��o ao inv�s de usar o nome.
 * Como dito, h� tamb�m um nome do mapa que neste caso � um nome virtual, al�m da dimens�o e objeto para mapear as c�lulas.</p>
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
	 * N�mero de identifica��o auto incremental no Map Cache.
	 */
	private int id;

	/**
	 * C�digo de identifica��o �nico por mapa em todos os servidores.
	 */
	private int mapIndex;

	/**
	 * Nome virtual para melhor identifica��o durante a cria��o do jogo.
	 */
	private String name;

	/**
	 * Objeto que especifica as dimens�es do mapa.
	 */
	private MapDimension dimension;

	/**
	 * Mapeamento das propriedades das c�lulas do mapa.
	 */
	private MapCell mapCell;

	/**
	 * Cria uma nova inst�ncia para armazenar dados de um mapa especifico.
	 * Inicializa o objeto de dimensionamento do mapa e mapeamento das c�lulas.
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
	 * @return aquisi��o do c�digo de identifica��o �nico por mapa.
	 */

	public int getMapIndex()
	{
		return mapIndex;
	}

	/**
	 * @param mapIndex c�digo de identifica��o �nico por mapa.
	 */

	public void setMapIndex(int mapIndex)
	{
		this.mapIndex = mapIndex;
	}

	/**
	 * @return aquisi��o do nome virtual do mapa.
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
	 * @return aquisi��o das dimens�es do mapa.
	 */

	public MapDimension getDimension()
	{
		return dimension;
	}

	/**
	 * @return aquisi��o do mapeamento das propriedades das c�lulas do mapa.
	 */

	public MapCell getMapCell()
	{
		return mapCell;
	}

	/**
	 * Atualiza o mapeamento das c�lulas com o dimensionamento atual do mapa.
	 * Em seguida inicializa o vetor que armazena as propriedades do mapa.
	 */

	public void initMapCell()
	{
		mapCell.setDimension(getDimension());
		mapCell.initData();
	}

	/**
	 * @return aquisi��o da quantidade de bytes que � gasta nas propriedades do mapa (c�lulas).
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
